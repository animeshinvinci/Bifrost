package com.datinko.asgard.bifrost

import _root_.io.scalac.amqp._
import akka.stream._
import com.typesafe.scalalogging.LazyLogging
import scaladsl._

/**
  * A Simple RabbitMQ consumer that connects to a queue and processes the messages received.
  * * (Note this class is so trivial that we probably dont need it but we will extend its functionality later).
  */
object SimpleRabbitConsumer extends LazyLogging {

  logger.debug("Setting up RabbitMQ Consumer...")

  //Creates a connection to RabbitMQ with sensible defaults.
  //They can be customised in application.conf
  val connection = Connection()

  def extractSentHeaderAndBody(delivery: Delivery): String = {

    // 'new String' feels wrong for scala.. not sure how else to do this..
    "sent: " + delivery.message.headers("sent") + " - body:" + new String(delivery.message.body.toArray)
    //"blah"
  }

  def consume() = {
    Source.fromPublisher(connection.consume("streams-playground"))
      .map(extractSentHeaderAndBody(_))
      .map(println(_))
      .to(Sink.ignore)  //this wont start consuming until run() is called.
                        //If we call 'run' here we'd need to pass a
                        //implicit flowMaterializer: FlowMaterializer
                        //into this method
  }


}