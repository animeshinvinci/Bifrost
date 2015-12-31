package com.datinko.asgard.bifrost

import akka.actor.Props
import akka.stream.{OverflowStrategy, ActorMaterializer, ClosedShape}
import akka.stream.scaladsl._
import com.datinko.asgard.bifrost.actors.{SlowDownActor, DelayingActor}
import io.scalac.amqp.Message
import scala.concurrent.duration._

/**
 * A set of test scenarios to demonstrate Akka Stream back pressure in action.  Metrics are exported
 * to StatsD by Kamon so they can be graphed in Grafana.
 */
object Scenarios {

  def fastPublisherFastSubscriber(implicit materializer: ActorMaterializer) = {

    val theGraph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[Unit] =>

      import GraphDSL.Implicits._

      val source = builder.add(ThrottledProducer.produceThrottled(materializer, 1 second, 20 milliseconds, 20000, "fastProducer"))
      val fastSink = builder.add(Sink.actorSubscriber(Props(classOf[DelayingActor], "fastSink")))

      source ~> fastSink

      ClosedShape
    })
    theGraph
  }


  def fastPublisherSlowingSubscriber(implicit materializer: ActorMaterializer) = {

    val theGraph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[Unit] =>

      import GraphDSL.Implicits._

      val source = builder.add(ThrottledProducer.produceThrottled(materializer, 1 second, 30 milliseconds, 20000, "fastProducer"))
      val slowingSink = builder.add(Sink.actorSubscriber(Props(classOf[SlowDownActor], "slowingDownSink", 10l)))

      source ~> slowingSink

      ClosedShape
    })
    theGraph
  }

  def fastPublisherSlowingSubscriberWithDropBuffer(implicit materializer: ActorMaterializer) = {

    val theGraph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[Unit] =>

      import GraphDSL.Implicits._

      val source = builder.add(ThrottledProducer.produceThrottled(materializer, 1 second, 30 milliseconds, 6000, "fastProducer"))
      val slowingSink = builder.add(Sink.actorSubscriber(Props(classOf[SlowDownActor], "slowingDownSink", 20l)))

      // now get the buffer, with 100 messages, that has an overflow
      // strategy that starts dropping the oldest messages in the buffer to fit new ones in when it is getting
      // too far behind.
      val buffer = Flow[Message].buffer(100, OverflowStrategy.dropHead)

      source ~> buffer ~> slowingSink

      ClosedShape
    })
    theGraph
  }

  def fastPublisherSlowingSubscriberWithBackPressure(implicit materializer: ActorMaterializer) = {

    val theGraph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[Unit] =>

      import GraphDSL.Implicits._

      val source = builder.add(ThrottledProducer.produceThrottled(materializer, 1 second, 30 milliseconds, 6000, "fastProducer"))
      val slowingSink = builder.add(Sink.actorSubscriber(Props(classOf[SlowDownActor], "slowingDownSink", 20l)))

      // now get the buffer, with 100 messages, with an overflow
      // strategy that signals backpressure when it is getting too far behind.
      // Our producer will respect this and slow down its production rate.
      val buffer = Flow[Message].buffer(100, OverflowStrategy.backpressure)

      source ~> buffer ~> slowingSink

      ClosedShape
    })
    theGraph
  }

}
