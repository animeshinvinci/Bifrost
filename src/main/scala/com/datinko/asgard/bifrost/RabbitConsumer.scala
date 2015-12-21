package com.datinko.asgard.bifrost

import akka.stream._
import scaladsl._
import io.scalac.amqp._

/**
  * A Simple RabbitMQ consumer that connects to a queue and processes the messages received
  */
object RabbitMqConsumer {


  //Creates a connection to RabbitMQ with sensible defaults.
  //They can be customised in application.conf
  val connection = Connection()

  def consume() = {
    Source(connection.consume("streams-playground"))
      .map(_.message.body.utf8String)
      .map(println(_))
      .to(Sink.ignore)  //this wont start consuming until run() is called.
                        //If we didn't do this we'd need to pass a
                        //implicit flowMaterializer: FlowMaterializer
                        //into this method
  }
}