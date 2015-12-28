package com.datinko.asgard.bifrost

import java.util.Calendar

import akka.stream.{SourceShape, ActorMaterializer}
import akka.stream.scaladsl.{Flow, Zip, GraphDSL, Source}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import io.scalac.amqp.Message
import kamon.Kamon

import scala.concurrent.duration.FiniteDuration

/**
 * An Akka Streams Source helper that produces AMQP messages at a defined rate.
 */
object ThrottledProducer extends LazyLogging {

  logger.debug("Setting up Throttled Producer...")

  def produceThrottled(implicit materializer: ActorMaterializer, initialDelay: FiniteDuration, interval: FiniteDuration, numberOfMessages: Int, name: String) = {

    val ticker = Source.tick(initialDelay, interval, Tick)
    val numbers = 1 to numberOfMessages
    val rangeMessageSource = Source(numbers.map(message => Message(ByteString(message), headers = Map("created" -> Calendar.getInstance().getTime().toString))))

    //define a stream to bring it all together..
    val throttledStream = Source.fromGraph(GraphDSL.create() { implicit builder =>

      //import this so we can use the ~> syntax
      import GraphDSL.Implicits._

      //define a zip operation that expects a tuple with a Tick and a Message in it..
      //(Note that the operations must be added to the builder before they can be used)
      val createCounter = Kamon.metrics.counter("throttledProducer-create-counter")
      val zip = builder.add(Zip[Tick.type, Message])

      //create a flow to extract the second element in the tuple (our message - we dont need the tick part after this stage)
      val messageExtractorFlow = builder.add(Flow[(Tick.type, Message)].map(_._2))

      //create a flow to log performance information to Kamon and pass on the message object unmolested
      val statsDExporterFlow = builder.add(Flow[(Message)].map{message => createCounter.increment(); message})

      //define the inputs for the zip function - it wont fire until something arrives at both inputs, so we are essentially
      //throttling the output of this steam
      ticker ~> zip.in0
      rangeMessageSource ~> zip.in1

      //send the output of our zip operation to a processing messageExtractorFlow that just allows us to take the second element of each Tuple, in our case
      //this is the AMQP message, we dont care about the Tick, it was just for timing and we can throw it away.
      //route that to the 'out' Sink, the RabbitMQ exchange.
      zip.out ~> messageExtractorFlow ~> statsDExporterFlow

      SourceShape(statsDExporterFlow.out)
    })
    throttledStream
  }

}
