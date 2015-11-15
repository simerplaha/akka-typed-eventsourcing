package commands

import akka.typed.ActorRef
import messages.Response


object UserAggregateCommands {

  trait UserCommand extends AggregateCommand {
    val replyTo: ActorRef[Response]
  }

  case class Initialize(username: String, name: String, password: String, replyTo: ActorRef[Response]) extends UserCommand

  case class UpdateName(name: String, replyTo: ActorRef[Response]) extends UserCommand

  case class ChangePassword(password: String, replyTo: ActorRef[Response]) extends UserCommand

  case class DeleteUser(username: String, replyTo: ActorRef[Response]) extends UserCommand

  case class GetState(replyTo: ActorRef[Response]) extends UserCommand

}