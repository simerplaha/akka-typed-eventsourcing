package read

import java.sql.Timestamp

import akka.typed.ScalaDSL._
import com.typesafe.scalalogging.LazyLogging
import events.{UserAggregateEvents, UserEvent}

object UserListener extends UserAggregateEvents with LazyLogging {

  /**
    * Receives UserEvent messages published by the write side.
    * @see main.ReadEventBus
    */

  val userListener: Static[(String, UserEvent, Timestamp)] = {
    Static[(String, UserEvent, Timestamp)] {
      case (id: String, event: UserCreated, timestamp: Timestamp) =>
        logger.info(s"Subscriber received message UserEvent -> $id : $event : $timestamp")
      case _ =>
        logger.info("Subscriber received some UserEvent message.")
    }
  }
}