package aggregate.base

import akka.typed.ScalaDSL._
import akka.typed.{ActorRef, ActorContext, Behavior, Props}
import com.typesafe.scalalogging.LazyLogging
import commands.{AggregateManagerCommand, AggregateCommand}
import events.AggregateEvent
import utils.ActorUtil

import messages.{State, ErrorMessage}

import scala.concurrent.Future

/**
  * Root aggregate manager for aggregates.
  * This sends commands to it's respective child aggregate.
  */

trait AggregateManager[AMC <: AggregateManagerCommand, AC <: AggregateCommand] extends ActorUtil with LazyLogging {

  /**
    * Processes each request in a Future block.
    */
  private val aggregateManager: Behavior[AMC] =
    ContextAware[AMC] {
      ctx =>
        SelfAware[AMC] {
          self =>
            implicit val executionContext = ctx.executionContext
            Static[AMC] {
              managerCommand =>
                Future(processCommand(ctx, managerCommand))
            }
        }
    }

  /**
    * Validates the command and fetches the command to be sent to child aggregate for processing or directly returns
    * error back to the client on validation error.
    */
  private def processCommand(ctx: ActorContext[AMC], managerCommand: AMC): Unit = {
    val childCommand = validateCommand(managerCommand)
    childCommand match {
      case Right(nextCommand) =>
        val child: ActorRef[AC] = getChild(ctx, managerCommand.id)
        child ! nextCommand
      case Left(error) =>
        managerCommand.replyTo ! error
    }
  }

  private def getChild(ctx: ActorContext[AMC], id: String): ActorRef[AC] =
    findOrCreate(ctx, childAggregate.props(id), id)

  protected def validateCommand(command: AMC): Either[ErrorMessage, AC]

  protected val childAggregate: Aggregate[AC, _, _]

  /**
    * Used by clients to to instantiate the extending AggregateManager.
    */
  def props: Props[AMC] = Props(aggregateManager)

}
