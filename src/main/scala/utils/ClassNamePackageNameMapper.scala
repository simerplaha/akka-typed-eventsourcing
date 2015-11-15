package utils

import messages.Event

import scala.reflect.runtime.universe._

/**
  * Used to create a Map of Event class with Event's name with package name.
  * This map is used to create Event object from the JSON stored in the Event store database.
  *
  * Map is of type [className:String, packageName:String]
  *
  * We could've just stored the full classname (with package name) in the database but that brings
  * in tight coupling between the database and application code.
  */
trait ClassNamePackageNameMapper[E <: Event] {
  type T

  def eventClassMapper(implicit tag: TypeTag[T]): Map[String, String] = typeOf[T].members.filter(_.isClass).map {
    member =>
      member.name.toString -> member.fullName.replace("." + member.name, "$" + member.name)
  }.toMap
}