package database

import com.github.tminglei.slickpg._

trait MyPostgresDriver extends ExPostgresDriver
with PgArraySupport
with PgDateSupport
with PgRangeSupport
with PgHStoreSupport
with PgSearchSupport
with PgNetSupport
with PgLTreeSupport {
  def pgjson = "jsonb"

  override val api = MyAPI

  object MyAPI extends API with ArrayImplicits
  with DateTimeImplicits
  with NetImplicits
  with LTreeImplicits
  with RangeImplicits
  with HStoreImplicits
  with SearchImplicits
  with SearchAssistants {
    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
  }

}

object MyPostgresDriver extends MyPostgresDriver