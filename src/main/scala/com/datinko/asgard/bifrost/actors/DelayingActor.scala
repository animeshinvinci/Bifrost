package com.datinko.asgard.bifrost.actors

import akka.stream.actor.ActorSubscriberMessage.{OnComplete, OnNext}
import akka.stream.actor.{OneByOneRequestStrategy, RequestStrategy, ActorSubscriber}

/**
 * Created by neild on 27/12/2015.
 */
//Actor Subscriber trait extension is need so that this actor can be used as part of a stream
class DelayingActor(name: String, delay: Long) extends ActorSubscriber {
  override protected def requestStrategy: RequestStrategy = OneByOneRequestStrategy

  val actorName = name

  import scala.collection.mutable

  val resultCollector = mutable.Map[Long, Double]()

  def this(name: String) {
    this(name, 0)
  }

  override def receive: Receive = {
    case OnNext(msg: Int) =>
      Thread.sleep(delay)
          println(s"In delaying actor sink $actorName: $msg")
    //      resultCollector += System.currentTimeMillis() -> msg

    case OnComplete =>
      println(s"Completed ${resultCollector.size}")
      //Util.sendToInfluxDB(Util.convertToInfluxDBJson(s"values-$actorName", resultCollector toMap))
  }
}
