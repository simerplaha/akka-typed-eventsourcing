package main

import akka.actor.Actor
import akka.typed.ActorRef
import commands.AggregateManagerCommand
import commands.UserManagerAggregateCommands._
import messages.Response
import org.json4s.DefaultFormats
import spray.httpx.Json4sSupport
import spray.routing._


class RoutingActor extends HttpService with Json4sSupport with Actor with RequestHandler {
  val json4sFormats = DefaultFormats

  val userRoute =
    path("createUser") {
      get {
        parameters('username, 'name, 'password) {
          (username: String, name: String, password: String) =>
            sendCommandToTypedSystem(CreateUser(username, username, name, password))
        }
      }
    } ~ path("updateName") {
      get {
        parameters('username, 'name) {
          (username: String, name: String) =>
            sendCommandToTypedSystem(UpdateName(username, name))
        }
      }
    } ~ path("changePassword") {
      get {
        parameters('username, 'password) {
          (username: String, password: String) =>
            sendCommandToTypedSystem(ChangePassword(username, password))
        }
      }
    } ~ path("deleteUser") {
      get {
        parameters('username) {
          (username: String) =>
            sendCommandToTypedSystem(DeleteUser(username))
        }
      }
    }

  val productRoute =
    path("addProduct") {
      get {
        parameters('name) {
          (name: String) =>
            sendCommandToTypedSystem(Product(name, name))
        }
      }
    }

  def actorRefFactory = context

  def receive = runRoute(userRoute ~ productRoute)

  /**
    * Sends all commands to the Akka-Typed's ActorSystem
    */

  def sendCommandToTypedSystem(partialCommand: ActorRef[Response] => AggregateManagerCommand): Route =
    requestContext =>
      systemTyped ! InitialCommand(requestContext, partialCommand)
}
