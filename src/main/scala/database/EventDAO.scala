package database

import database.DBConfig.slickDriver.api._
import database.Schema.events
import domain.PersistentEvent

object EventDAO {

  val database = DBConfig.database

  def createEvent(event: PersistentEvent) =
    database.run(events += event)

  def getEvents(id: String) = {
    val query = events.filter(_.persistentId === id).sortBy(_.createTime)
    database.run(query.result)
  }
}
