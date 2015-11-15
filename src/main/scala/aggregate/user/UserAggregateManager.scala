package aggregate.user

import aggregate.base.AggregateManager
import commands.UserAggregateCommands
import commands.UserAggregateCommands.{Initialize, UserCommand}
import commands.UserManagerAggregateCommands._
import events.UserEvent
import messages.ErrorMessage


/**
  * Root manager for User commands.
  */

object UserAggregateManager extends AggregateManager[UserManagerCommand, UserCommand, UserEvent, UserState] {

  /**
    * Validates command and returns an Error or a Command for the child UserAggregate to process.
    */

  protected override def validateCommand(command: UserManagerCommand): Either[ErrorMessage, UserCommand] = {

    command match {
      case CreateUser(_, username, name, password) =>
        Right(Initialize(username, name, password, command.replyTo))
      case UpdateName(_, name) =>
        Right(UserAggregateCommands.UpdateName(name, command.replyTo))
      case ChangePassword(_, password) =>
        Right(UserAggregateCommands.ChangePassword(password, command.replyTo))
      case DeleteUser(_) =>
        Right(UserAggregateCommands.DeleteUser(command.id, command.replyTo))
    }
  }

  /**
    * Informs the AggregateManager trait about the child aggregate object's instance.
    */
  protected override val childAggregate = UserAggregate
}


