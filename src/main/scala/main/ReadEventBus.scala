package main

import java.sql.Timestamp

import akka.actor._
import akka.event.{EventBus, SubchannelClassification}
import akka.util.Subclassification

object ReadEventBus extends EventBus with SubchannelClassification {
  type Event = (String, Any, Timestamp)
  type Classifier = Class[_]
  type Subscriber = ActorRef

  override protected def classify(event: Event): Classifier = event._2.getClass

  protected def subclassification = new Subclassification[Classifier] {
    def isEqual(x: Classifier, y: Classifier) = x == y

    def isSubclass(x: Classifier, y: Classifier) = x.isAssignableFrom(y) || y.isAssignableFrom(x)
  }

  override protected def publish(event: Event, subscriber: Subscriber): Unit = subscriber ! event
}