package aggregate.base

import java.sql.Timestamp

import akka.typed.ScalaDSL._
import akka.typed._
import com.google.gson.Gson
import com.typesafe.scalalogging.LazyLogging
import commands.AggregateCommand
import database.EventDAO
import domain.PersistentEvent
import events.AggregateEvent
import messages.{Response, State}
import utils.ActorUtil

import scala.annotation.tailrec


trait Aggregate[C <: AggregateCommand, E <: AggregateEvent, S <: State] extends ActorUtil with LazyLogging {

  protected case class NextBehaviorAndCurrentState(currentState: S, behavior: Behavior[C])

  private val eventDAO = EventDAO

  private val gson = new Gson()

  val emptyList = List.empty[String]

  /**
    * Persist's the event to database and updates the state.
    */
  protected def persist(id: String,
                        event: E,
                        previousState: S,
                        context: ActorContext[C],
                        tags: List[String] = emptyList): NextBehaviorAndCurrentState = {
    //    val eventJson = compact(render(decompose(event)))
    val eventJson = gson.toJson(event)
    val persistenceEvent = PersistentEvent(id, eventJson, event.getClass.getSimpleName, tags, new Timestamp(System.currentTimeMillis()))
    eventDAO.createEvent(persistenceEvent)
    context.system.eventStream.publish(event)
    logger.info(s"Applying event: $event to ${this.getClass.getSimpleName}(id = '$id')")
    applyEvent(id, event, previousState)
  }

  /**
    * Persist's the event to database, updates the state and responds back to the user.
    * Current state is returned as response if no Response is set.
    */
  protected def persistAndRespond(id: String,
                                  event: E,
                                  previousState: S,
                                  replyTo: ActorRef[Response],
                                  context: ActorContext[C],
                                  response: Option[Response] = None,
                                  tags: List[String] = emptyList): NextBehaviorAndCurrentState = {
    val nextBehaviorCurrentState = persist(id, event, previousState, context, tags)
    replyTo ! response.getOrElse(nextBehaviorCurrentState.currentState)
    nextBehaviorCurrentState
  }

  /**
    * Entry behavior for every aggregate.
    * 1. Starts in recovery mode
    * 2. Fetches all the EVENTS from the database for the id
    * 3. Replays all events to restore it's state.
    */
  private def recover(id: String): Behavior[C] = {
    logger.info(s"Recovering state for Aggregate: ${this.getClass.getSimpleName}(id = '$id')")
    Full[C] {
      case Sig(_, PreStart) =>
        val pEvents = eventDAO.getEvents(id)
        val events = pEvents map {
          persistentEvent =>
            toEvent(persistentEvent.json, persistentEvent.eventName)
        }
        playEvents(id, events, initialState, uninitialized(id))
    }
  }

  /**
    * Recursively plays all the events to restore aggregate's state.
    */
  @tailrec
  private def playEvents(id: String, events: List[E], state: S, currentBehavior: Behavior[C]): Behavior[C] = {
    events match {
      case Nil =>
        logger.info(s"Finished playingEvents in recovery mode for: ${this.getClass.getSimpleName}(id = '$id')")
        currentBehavior
      case event :: remainingEvents =>
        val stateAndNextBehavior = applyEvent(id, event, state)
        playEvents(id, remainingEvents, stateAndNextBehavior.currentState, stateAndNextBehavior.behavior)
    }
  }

  /**
    * Maps JSON string to Event object.
    */
  private def toEvent(json: String, eventClassName: String): E = {
    val fullClassName = getFullClassName(eventClassName)
    val clazz = Class.forName(fullClassName)
    gson.fromJson(json, clazz).asInstanceOf[E]
  }

  protected def applyEvent(id: String, event: E, state: S): NextBehaviorAndCurrentState

  /**
    * Expects a full class name (with package name) for the passed in Event class name.
    */
  protected def getFullClassName(className: String): String

  protected def uninitialized(id: String): Behavior[C]

  protected val initialState: S

  def props(id: String) = Props(recover(id))
}
