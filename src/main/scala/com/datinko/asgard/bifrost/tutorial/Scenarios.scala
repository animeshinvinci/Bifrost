package com.datinko.asgard.bifrost.tutorial

import akka.actor.Props
import akka.stream.{ClosedShape}
import akka.stream.scaladsl.{Sink, GraphDSL, RunnableGraph}
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
}
