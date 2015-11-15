package commands

import akka.typed.ActorRef
import messages.{ErrorMessage, Response}

import scala.collection.mutable.ListBuffer


object UserManagerAggregateCommands {

  trait UserManagerCommand extends AggregateManagerCommand

  case class CreateUser(id: String, username: String, name: String, password: String)(val replyTo: ActorRef[Response]) extends UserManagerCommand {
    val validate: List[ErrorMessage] = {
      val errors = ListBuffer.empty[ErrorMessage]
      if (id == "")
        errors += ErrorMessage(s"Not a valid id: $id.")
      if (name == "")
        errors += ErrorMessage(s"Not a valid name: $name.")
      if (password == "")
        errors += ErrorMessage(s"Not a valid password: $password.")
      errors.toList
    }

  }

  case class UpdateName(id: String, name: String)(val replyTo: ActorRef[Response]) extends UserManagerCommand {
    val validate: List[ErrorMessage] = {
      val errors = ListBuffer.empty[ErrorMessage]
      if (id == "")
        errors += ErrorMessage(s"Not a valid id: $id.")
      if (name == "")
        errors += ErrorMessage(s"Not a valid name: $name.")
      errors.toList
    }
  }

  case class ChangePassword(id: String, password: String)(val replyTo: ActorRef[Response]) extends UserManagerCommand {
    val validate: List[ErrorMessage] = {
      val errors = ListBuffer.empty[ErrorMessage]
      if (id == "")
        errors += ErrorMessage(s"Not a valid id: $id.")
      if (password == "")
        errors += ErrorMessage(s"Not a valid password: $password.")
      errors.toList
    }
  }

  case class DeleteUser(id: String)(val replyTo: ActorRef[Response]) extends UserManagerCommand {
    val validate: List[ErrorMessage] = {
      val errors = ListBuffer.empty[ErrorMessage]
      if (id == "")
        errors += ErrorMessage(s"Not a valid id: $id.")
      errors.toList
    }
  }

}