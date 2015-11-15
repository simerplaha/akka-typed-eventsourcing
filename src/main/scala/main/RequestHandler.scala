package main

import aggregate.user.UserAggregateManager
import akka.typed.ScalaDSL._
import akka.typed._
import com.typesafe.scalalogging.LazyLogging
import commands.UserManagerAggregateCommands.UserManagerCommand
import commands.{AggregateManagerCommand, UserManagerAggregateCommands}
import messages.{Message, _}
import spray.http.StatusCodes._
import spray.httpx.Json4sSupport
import spray.routing.RequestContext
import utils.{ActorUtil, UUIDGenerator}

case class InitialCommand(requestContext: RequestContext, partialCommand: ActorRef[Response] => AggregateManagerCommand)

case class Product(id: String, name: String)(val replyTo: ActorRef[Response]) extends AggregateManagerCommand {
  override val validate: List[ErrorMessage] = List.empty[ErrorMessage]
}


trait RequestHandler extends ActorUtil with Json4sSupport with LazyLogging {


  val systemTyped = ActorSystem("actor-system-typed", Props(rootGuardian))


  /**
    * Root guardian - Sends the command to it's relevant AggregateManager.
    * Also sets replyTo attribute to the responseHandler Behaviour
    */
  def rootGuardian =
    ContextAware[InitialCommand] {
      ctx =>
        val minimalResponseActor = ctx.spawn(Props(minimalResponseBehavior), "minimalResponseActor")
        val userAggregateManager = ctx.spawn(UserAggregateManager.props, "userAggregateManager")

        Static[InitialCommand] {
          initialCommand =>
            initialCommand.partialCommand(minimalResponseActor) match {
              case command: UserManagerCommand =>
                val requestHandlerActor = ctx.spawn(Props(userRequestHandler(initialCommand.requestContext, userAggregateManager)), UUIDGenerator.uuid)
                requestHandlerActor ! command
              case command: Product =>
                initialCommand.requestContext.complete(OK, Message("Not implemented yet."))
            }
        }
    }

  /**
    * Implements Actor per request pattern.
    * Request handler for 'User manager commands'. It does some preliminary command validation and forwards the
    * command to the Root command manager for User (UserAggregateManager)
    */
  def userRequestHandler(requestContext: RequestContext, userAggregateManager: ActorRef[UserManagerCommand]) = {
    import UserManagerAggregateCommands._
    SelfAware[Response] {
      self =>

        def processCommand(command: UserManagerCommand): Behavior[Response] = {
          val errors = command.validate
          if (errors.nonEmpty) {
            requestContext.complete(BadRequest, errors)
            Stopped
          } else {
            userAggregateManager ! command
            responseHandler(requestContext)
          }
        }

        Total[Response] {
          case command: CreateUser =>
            processCommand(command.copy()(self))
          case command: UpdateName =>
            processCommand(command.copy()(self))
          case command: ChangePassword =>
            processCommand(command.copy()(self))
          case command: DeleteUser =>
            processCommand(command.copy()(self))
        }
    }
  }

  /**
    * Handles system response to display to user.
    */
  def responseHandler(requestContext: RequestContext) = {
    Total[Response] {
      case success: Message =>
        requestContext.complete(OK, success)
        Stopped
      case ack: Acknowledge =>
        requestContext.complete(OK, ack)
        Stopped
      case error: ErrorMessage =>
        requestContext.complete(BadRequest, error)
        Stopped
      case state: State =>
        requestContext.complete(OK, state)
        Stopped
      case timeout: Timeout =>
        requestContext.complete(RequestTimeout, timeout)
        Stopped
      case _ =>
        requestContext.complete(BadRequest, ErrorMessage("Got invalid response."))
        Stopped
    }
  }

  /**
    * TODO: Find better a approach.
    *
    * Used to get the full command from the partialCommand in InitialCommand case class to make it easier to do pattern
    * matching of command type.
    *
    * Just though this is a simpler approach then passing around Types or doing pattern matching on Functions
    * with TypeTag.
    */
  val minimalResponseBehavior = {
    Static[Response] {
      case _ =>
        logger.error("WOAH! I got a message .... I should not be handling any messages.")
    }
  }
}
