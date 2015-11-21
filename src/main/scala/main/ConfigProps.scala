package main

import com.typesafe.config.ConfigFactory

object ConfigProps {

  val dbConfig = ConfigFactory.load().getConfig("db.config")

  val queryTimeout = dbConfig.getInt("timeout")
}
