package database

import java.sql.Timestamp

import domain.PersistentEvent
import messages.Event

import scala.concurrent.Await
import scala.concurrent.duration._

import MyPostgresDriver.api._

object Schema {

  import DBConfig.slickDriver.api._

  val database = DBConfig.database


  class Events(tag: Tag) extends Table[PersistentEvent](tag, "EVENTS") {

    def persistentId = column[String]("PERSISTENT_ID")

    def json = column[String]("EVENT_JSON")

    def tags = column[List[String]]("TAGS", O.Default(Nil))

    def eventName = column[String]("EVENT_NAME")

    def createTime = column[Timestamp]("CREATE_TIME")

    def idx = index("persistence_id_index", persistentId)

    def tagsIndex = index("tags_index", tags)

    def createTimeIndex = index("create_time_index", createTime)


    def * = (persistentId, json, eventName, tags, createTime) <>(
      (resultSet: (String, String, String, List[String], Timestamp)) => PersistentEvent(resultSet._1, resultSet._2, resultSet._3, resultSet._4, resultSet._5),
      (event: PersistentEvent) => Some((event.persistentId, event.json, event.eventName, event.tags, event.createTime)))
  }

  val events = TableQuery[Events]

  def createSchema() = {

    val setup = DBIO.seq(
      events.schema.create
      //      events += PersistentEvent(id, some_json, tags, time)
    )
    Await.result(database.run(setup), 10 seconds)
  }

  def main(args: Array[String]) = createSchema()

}


