package aggregate.base

import akka.typed.ScalaDSL._
import akka.typed._
import com.typesafe.scalalogging.LazyLogging
import commands.{AggregateCommand, AggregateManagerCommand}
import messages.ErrorMessage
import utils.ActorUtil

/**
  * Root aggregate manager for aggregates.
  * This sends commands to it's respective child aggregate.
  */

trait AggregateManager[AMC <: AggregateManagerCommand, AC <: AggregateCommand] extends ActorUtil with LazyLogging {

  /**
    * Processes each request.
    */
  private val aggregateManager: Behavior[AMC] =
    Full[AMC] {
      case Msg(ctx, command: AMC) =>
        logger.info(s"Processing command in ${this.getClass.getSimpleName}:" + command)
        processCommand(ctx, command)
        Same
      case Sig(_, failed: Failed) =>
        logger.error("Error running child Actor! Please check for exceptions - Restarting child actor:", failed.cause)
        failed.decide(Failed.Stop)
        Same
    }

  /**
    * Validates the command and fetches the command to be sent to child aggregate for processing or directly returns
    * error back to the client on validation error.
    */
  private def processCommand(ctx: ActorContext[AMC], managerCommand: AMC): Unit = {
    val childCommand = validateCommand(managerCommand)
    childCommand match {
      case Right(childCommand) =>
        val child: ActorRef[AC] = getChild(ctx, managerCommand.id)
        ctx.watch(child)
        child ! childCommand
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
