package com.datinko.asgard.bifrost.actors

import akka.stream.actor.ActorSubscriberMessage.{OnComplete, OnNext}
import akka.stream.actor.{OneByOneRequestStrategy, RequestStrategy, ActorSubscriber}
import com.typesafe.scalalogging.LazyLogging
import _root_.io.scalac.amqp.{Message}
import kamon.Kamon


/**
 * Created by neild on 27/12/2015.
 */
//Actor Subscriber trait extension is need so that this actor can be used as part of a stream
class DelayingActor(name: String, delay: Long) extends ActorSubscriber with LazyLogging {
  override protected def requestStrategy: RequestStrategy = OneByOneRequestStrategy

  val actorName = name

  import scala.collection.mutable

  //val resultCollector = mutable.Map[Long, Double]()
  val consumeCounter = Kamon.metrics.counter("delayingactor-consumed-counter")

  def this(name: String) {
    this(name, 0)
  }

  override def receive: Receive = {
    case OnNext(msg: Message) =>
      Thread.sleep(delay)
      logger.debug(s"Message in delaying actor sink ${self.path} '$actorName': $msg")
      consumeCounter.increment()


    //println(s"In delaying actor sink $actorName: $msg")
    //      resultCollector += System.currentTimeMillis() -> msg

    case OnComplete =>
      //logger.debug(s"Completed ${resultCollector.size}")
      //println(s"Completed ${resultCollector.size}")
      //Util.sendToInfluxDB(Util.convertToInfluxDBJson(s"values-$actorName", resultCollector toMap))

    case _ =>
      logger.debug(s"Unknown message in  $actorName: ")
      //println(s"Unknown message in delaying actor sink $actorName: ")

  }
}
