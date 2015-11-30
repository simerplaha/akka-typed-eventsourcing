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
    * @param event Event to apply to state
    * @param oldState previous state of the User
    * @return UserState - returns User's new state.
    */
  protected override def updateState(event: UserEvent, oldState: UserState): UserState = {
    event match {
      case UserCreated(username, name, password) =>
        oldState.copy(username, name, password)
      case UserNameUpdated(name) =>
        oldState.copy(name = name)
      case UserPasswordChanged(password) =>
        oldState.copy(password = password)
      case UserDeleted() =>
        oldState.copy(deleted = true)
    }
  }

  /**
    * Returns the appropriate Behavior for a state
    */
  protected override def getBehavior(id: String, state: UserState): Behavior[UserCommand] = {
    if (state == initialState)
      uninitialized(id)
    else if (state.deleted)
      deleted(id, state)
    else
      created(id, state)
  }

  /**
    * Initializes a user by create a UserCreated event.
    */
  protected def uninitialized(id: String): Behavior[UserCommand] =
    ContextAware[UserCommand] {
      ctx =>
        Total[UserCommand] {
          case Initialize(username, name, password, replyTo) =>
            val event = UserCreated(username, name, password)
            persistAndRespond(id, event, initialState, replyTo, ctx)
          case error: UserCommand =>
            error.replyTo ! ErrorMessage(s"User not initialized yet! Invalid command: '${error.getClass.getSimpleName}'")
            Stopped
        }
    }

  /**
    * Implements rules for 'created' state of a User.
    */
  private def created(id: String, user: UserState): Behavior[UserCommand] =
    ContextAware[UserCommand] {
      ctx =>
        Total[UserCommand] {
          case GetState(replyTo) =>
            replyTo ! user
            Same
          case UpdateName(name, replyTo) =>
            if (name == user.name) {
              replyTo ! Message("Name unchanged!", Some(user))
              Same
            } else {
              persistAndRespond(id, UserNameUpdated(name), user, replyTo, ctx)
            }
          case ChangePassword(password, replyTo) =>
            persistAndRespond(id, UserPasswordChanged(password), user, replyTo, ctx)
          case DeleteUser(name, replyTo) =>
            persistAndRespond(id, UserDeleted(), user, replyTo, ctx)
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
  private def deleted(id: String, user: UserState): Behavior[UserCommand] =
    ContextAware[UserCommand] {
      ctx =>
        Total[UserCommand] {
          case GetState(replyTo) =>
            replyTo ! user
            Same
          case DeleteUser(username, replyTo) =>
            persistAndRespond(id, UserDeleted(), user, replyTo, ctx)
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
