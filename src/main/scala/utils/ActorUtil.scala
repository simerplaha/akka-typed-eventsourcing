package utils

import akka.typed.{ActorContext, ActorRef, Props}

/**
  * Helper class for Akka actor
  */

trait ActorUtil {

  /**
    * Finds or creates an ActorRef[T] with id.
    */
  protected def findOrCreate[T](context: ActorContext[_],
                                props: Props[T],
                                id: String): ActorRef[T] = {
    val childMayBe = context.child(id)
    if (childMayBe.isDefined)
      childMayBe.get.asInstanceOf[ActorRef[T]]
    else
      context.spawn(props, id)
  }
}
