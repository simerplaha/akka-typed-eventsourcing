package main

import akka.typed._
import events.UserEvent
import read.UserListener

object EventSubscribers {

  def initialize(ctx: ActorContext[_]): Unit = {
    val userEventListener = ctx.spawn(Props(UserListener.userListener), "userEventListener")

    val untypedActor = classOf[ActorRef[_]].getDeclaredMethod("untypedRef").invoke(userEventListener).asInstanceOf[akka.actor.ActorRef]
    ReadEventBus.subscribe(untypedActor, classOf[UserEvent])
  }
}
