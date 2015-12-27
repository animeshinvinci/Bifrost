package com.datinko.asgard.bifrost

import java.util.Calendar

import _root_.io.scalac.amqp.Message
import akka.actor.Props
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl._
import akka.util.ByteString
import com.datinko.asgard.bifrost.actors.DelayingActor
import scala.concurrent.duration._
import akka.stream._


/**
 * Created by neild on 27/12/2015.
 */
object Scenarios {

  def fastPublisherFastSubscriber(implicit materializer: ActorMaterializer) = {

    val theGraph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[Unit] =>

      import GraphDSL.Implicits._

      val source = builder.add(SimpleRabbitProducer.produceThrottled(materializer, 1 second, 20 milliseconds, 20000, "fastProducer"))
      val fastSink = builder.add(Sink.actorSubscriber(Props(classOf[DelayingActor], "fastSink")))

      //these are red because the source isn't actually a SourceShape
      source ~> fastSink

      ClosedShape
    })
    theGraph
  }

}
