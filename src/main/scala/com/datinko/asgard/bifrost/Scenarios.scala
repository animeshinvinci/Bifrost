package com.datinko.asgard.bifrost

import akka.actor.Props
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl._
import com.datinko.asgard.bifrost.actors.{SlowDownActor, DelayingActor}
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

      //these are red because the source isn't actually a SourceShape
      source ~> fastSink

      ClosedShape
    })
    theGraph
  }

  def fastPublisherSlowingSubscriber(implicit materializer: ActorMaterializer) = {

    val theGraph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[Unit] =>

      import GraphDSL.Implicits._

      val source = builder.add(ThrottledProducer.produceThrottled(materializer, 1 second, 20 milliseconds, 20000, "fastProducer"))
      val slowingSink = builder.add(Sink.actorSubscriber(Props(classOf[SlowDownActor], "slowingDownSink", 10l)))

      source ~> slowingSink

      ClosedShape
    })
    theGraph
  }

}
