package domain

import java.sql.Timestamp

case class PersistentEvent(persistentId: String,
                           json: String,
                           eventName: String,
                           tags: List[String],
                           createTime: Timestamp)
