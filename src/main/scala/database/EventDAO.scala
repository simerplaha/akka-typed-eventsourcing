package database

import database.DBConfig.slickDriver.api._
import database.Schema.events
import domain.PersistentEvent

import scala.concurrent.Await
import scala.concurrent.duration._

object EventDAO {

  val database = DBConfig.database

  def createEvent(event: PersistentEvent) = {
    Await.result(database.run(events += event), 10 seconds)
  }

  def getEvents(id: String): List[PersistentEvent] = {
    val query = events.filter(_.persistentId === id).sortBy(_.createTime)
    Await.result(database.run(query.result), 10 seconds).toList
  }
}
