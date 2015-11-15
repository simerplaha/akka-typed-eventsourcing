package main

import akka.actor._
import akka.io.IO
import com.typesafe.scalalogging.LazyLogging
import spray.can.Http

object Boot extends App with LazyLogging {

  logger.info("******** Starting application ********")

  implicit val system = ActorSystem("actor-system")

  implicit val executionContext = system.dispatcher

  val service = system.actorOf(Props(new RoutingActor), "routing-actor")

  IO(Http) ! Http.Bind(service, interface = "0.0.0.0", port = 8080)
}
