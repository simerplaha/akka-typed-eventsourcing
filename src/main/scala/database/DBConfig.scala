package database

import slick.driver.H2Driver.api._
import slick.driver.PostgresDriver

object DBConfig {
  lazy val slickDriver = PostgresDriver
  lazy val database = Database.forConfig("postgres")
}
