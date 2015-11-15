package events

import utils.ClassNamePackageNameMapper

trait UserEvent extends AggregateEvent

/**
  * Implements only the events for this Aggregate.
  * Extends ClassNamePackageNameMapper to generates a Map of [className:String, packageName:String]
  * This map is used to map EVENT_NAME to the respective Event class objects.
  */
trait UserAggregateEvents extends ClassNamePackageNameMapper[UserEvent] {
  override type T = this.type

  case class UserCreated(username: String, name: String, password: String) extends UserEvent

  case class UserNameUpdated(name: String) extends UserEvent

  case class UserDeleted() extends UserEvent

  case class UserPasswordChanged(password: String) extends UserEvent

}
