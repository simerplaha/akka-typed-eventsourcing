package commands

import akka.typed.ActorRef
import messages.{Command, Response, ErrorMessage}

trait AggregateManagerCommand extends Command {
  val id: String
  val replyTo: ActorRef[Response]
  val validate: List[ErrorMessage]
}
