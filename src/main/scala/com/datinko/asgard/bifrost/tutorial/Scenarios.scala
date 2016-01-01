package com.datinko.asgard.bifrost.tutorial

import akka.actor.Props
import akka.stream.{OverflowStrategy, ClosedShape}
import akka.stream.scaladsl.{Flow, Sink, GraphDSL, RunnableGraph}
import com.datinko.asgard.bifrost.tutorial.actors.SlowDownActor
import io.scalac.amqp.Message
import scala.concurrent.duration._
/**
 * A set of test scenarios to demonstrate Akka Stream back pressure in action.  Metrics are exported
 * to StatsD by Kamon so they can be graphed in Grafana.
 */
object Scenarios {

  def fastPublisherFastSubscriber() = {

    val theGraph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>

      val source = builder.add(ThrottledProducer.produceThrottled(1 second, 20 milliseconds, 20000, "fastProducer"))
      val fastSink = builder.add(Sink.actorSubscriber(Props(classOf[actors.DelayingActor], "fastSink")))

      import GraphDSL.Implicits._

      source ~> fastSink

      ClosedShape
    })
    theGraph
  }

  def fastPublisherSlowingSubscriber() = {

    val theGraph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[Unit] =>

      val source = builder.add(ThrottledProducer.produceThrottled(1 second, 30 milliseconds, 20000, "fastProducer"))
      val slowingSink = builder.add(Sink.actorSubscriber(Props(classOf[SlowDownActor], "slowingDownSink", 50l)))

      import GraphDSL.Implicits._

      source ~> slowingSink

      ClosedShape
    })
    theGraph
  }

  def fastPublisherSlowingSubscriberWithDropBuffer() = {

    val theGraph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[Unit] =>

      val source = builder.add(ThrottledProducer.produceThrottled(1 second, 30 milliseconds, 6000, "fastProducer"))
      val slowingSink = builder.add(Sink.actorSubscriber(Props(classOf[SlowDownActor], "slowingDownSink", 50l)))

      // create a buffer, with 100 messages, with an overflow
      // strategy that starts dropping the oldest messages when it is getting
      // too far behind.
      val bufferFlow = Flow[String].buffer(100, OverflowStrategy.dropHead)

      import GraphDSL.Implicits._

      source ~> bufferFlow ~> slowingSink

      ClosedShape
    })
    theGraph
  }

  def fastPublisherSlowingSubscriberWithBackpressureBuffer() = {

    val theGraph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[Unit] =>

      val source = builder.add(ThrottledProducer.produceThrottled(1 second, 30 milliseconds, 6000, "fastProducer"))
      val slowingSink = builder.add(Sink.actorSubscriber(Props(classOf[SlowDownActor], "slowingDownSink", 50l)))

      // create a buffer, with 100 messages, with an overflow
      // strategy that starts dropping the oldest messages when it is getting
      // too far behind.
      val bufferFlow = Flow[String].buffer(100, OverflowStrategy.backpressure)

      import GraphDSL.Implicits._

      source ~> bufferFlow ~> slowingSink

      ClosedShape
    })
    theGraph
  }
}
