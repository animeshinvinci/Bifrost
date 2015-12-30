package com.datinko.asgard.bifrost.tutorial

import akka.actor.Props
import akka.stream.{ClosedShape, ActorMaterializer}
import akka.stream.scaladsl._
import com.datinko.asgard.bifrost.ThrottledProducer
import com.datinko.asgard.bifrost.actors.DelayingActor
import io.scalac.amqp.Message
import scala.concurrent.duration._

/**
 * An examples of the simplest possible akka stream.
 */
object SimpleStreams {

  def printSimpleMessagesToConsole(implicit materializer: ActorMaterializer) = {

    val simpleMessages = "Message 1" :: "Message 2" :: "Message 3" :: "Message 4" :: "Message 5" :: Nil

    Source(simpleMessages)
      .map(println(_))
      .to(Sink.ignore)
      .run()
  }

  def throttledProducerToConsole(implicit materializer: ActorMaterializer) = {

    val theGraph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[Unit] =>

      import GraphDSL.Implicits._

      val source = builder.add(ThrottledProducer.produceThrottled(materializer, 1 second, 20 milliseconds, 20000, "fastProducer"))
      val printFlow = builder.add(Flow[(Message)].map{println(_)})

      val sink = builder.add(Sink.ignore)

      source ~> printFlow ~> sink

      ClosedShape
    })
    theGraph
  }
}
