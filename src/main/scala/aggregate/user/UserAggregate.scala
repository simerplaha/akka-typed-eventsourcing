package aggregate.user

import aggregate.base.Aggregate
import akka.typed.Behavior
import akka.typed.ScalaDSL._
import commands.UserAggregateCommands._
import events.{UserAggregateEvents, UserEvent}
import messages._


case class UserState(username: String,
                     name: String,
                     password: String,
                     deleted: Boolean) extends State

/**
  * Finite state machine for Users.
  */
object UserAggregate extends Aggregate[UserCommand, UserEvent, UserState] with UserAggregateEvents {

  /**
    * State updater
    * @param id id of this group of events
    * @param event Event to apply to state
    * @param oldState previous state of the User
    * @return NextBehaviorAndCurrentState - stores the current state of User and the next behavior.
    */
  protected override def applyEvent(id: String, event: UserEvent, oldState: UserState): NextBehaviorAndCurrentState = {
    event match {
      case UserCreated(username, name, password) =>
        val newState = oldState.copy(username, name, password)
        NextBehaviorAndCurrentState(newState, created(id, newState))
      case UserNameUpdated(name) =>
        val newUserState = oldState.copy(name = name)
        NextBehaviorAndCurrentState(newUserState, created(id, newUserState))
      case UserPasswordChanged(password) =>
        val newUserState = oldState.copy(password = password)
        NextBehaviorAndCurrentState(newUserState, created(id, newUserState))
      case UserDeleted() =>
        val newState = oldState.copy(deleted = true)
        NextBehaviorAndCurrentState(newState, deleted(id, newState))
    }
  }

  /**
    * Initializes a user by create a UserCreated event.
    */
  protected def uninitialized(id: String): Behavior[UserCommand] = {
    Total[UserCommand] {
      case Initialize(username, name, password, replyTo) =>
        val event = UserCreated(username, name, password)
        persistAndRespond(id, event, initialState, replyTo).behavior
      case error: UserCommand =>
        error.replyTo ! ErrorMessage(s"User not initialized yet! Invalid command: '${error.getClass.getSimpleName}'")
        Stopped
    }
  }

  /**
    * Implements rules for 'created' state of a User.
    */
  private def created(id: String, user: UserState): Behavior[UserCommand] = {
    Total[UserCommand] {
      case GetState(replyTo) =>
        replyTo ! user
        Same
      case UpdateName(name, replyTo) =>
        if (name == user.name) {
          replyTo ! Message("Name unchanged!", Some(user))
          Same
        } else {
          persistAndRespond(id, UserNameUpdated(name), user, replyTo).behavior
        }
      case ChangePassword(password, replyTo) =>
        persistAndRespond(id, UserPasswordChanged(password), user, replyTo).behavior
      case DeleteUser(name, replyTo) =>
        persistAndRespond(id, UserDeleted(), user, replyTo).behavior
      case Initialize(username, name, password, replyTo) =>
        replyTo ! ErrorMessage(s"Username '$id' with name '$name' already exists.")
        Same
      case unhandledCommand: UserCommand =>
        unhandledCommand.replyTo ! ErrorMessage(s"Not a valid request: '${unhandledCommand.getClass.getSimpleName}' for current state: 'created'", Some(user))
        Same
    }
  }

  /**
    * Implements rules for 'deleted' state of a User.
    */
  private def deleted(id: String, user: UserState): Behavior[UserCommand] = {
    Total[UserCommand] {
      case GetState(replyTo) =>
        replyTo ! user
        Same
      case DeleteUser(username, replyTo) =>
        persistAndRespond(id, UserDeleted(), user, replyTo).behavior
      case unhandledCommand: UserCommand =>
        unhandledCommand.replyTo ! ErrorMessage(s"Not a valid request: '${unhandledCommand.getClass.getSimpleName}' for current state: 'deleted'", Some(user))
        Same
    }
  }

  /**
    * Returns a Full classname for the passed Event name.
    */
  protected def getFullClassName(eventClassName: String): String =
    eventClassMapper.get(eventClassName).get

  /**
    * Initial state of every User.
    */
  protected override val initialState: UserState =
    UserState(
      username = "",
      name = "",
      password = "",
      deleted = false
    )
}
