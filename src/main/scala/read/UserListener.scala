package read

import akka.typed.ScalaDSL._
import events.{UserAggregateEvents, UserEvent}

object UserListener extends UserAggregateEvents {

  val userListener = {
    Static[(String, UserNameUpdated)] {
      case (id, UserNameUpdated(name
      )) =>
        println(id + ":" + name)
    }
  }
}
