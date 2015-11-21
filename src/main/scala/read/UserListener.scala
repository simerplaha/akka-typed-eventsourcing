package read

import java.sql.Timestamp

import akka.typed.ScalaDSL._
import com.typesafe.scalalogging.LazyLogging
import events.{UserAggregateEvents, UserEvent}

object UserListener extends UserAggregateEvents with LazyLogging {

  val userListener = {
    Static[(String, UserEvent, Timestamp)] {
      case (id: String, event: UserCreated, timestamp: Timestamp) =>
        logger.info(s"Subscriber -> $id : $event : $timestamp")
      case _ =>
        logger.info("Got some other UserEvent")
    }
  }
}