package com.datinko.asgard.bifrost

import java.util.Calendar

import _root_.io.scalac.amqp.{Message, Connection}
import akka.stream.{SourceShape, ActorMaterializer}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import akka.stream.scaladsl._

/**
  * A simple RabbitMQProducer to send a defined series of messages to a RabbitMQ Exchange.
  * (Note this class is so trivial that we probably dont need it but we will extend its functionality later).
  */
object SimpleRabbitProducer extends LazyLogging {

  logger.debug("Setting up RabbitMQ Producer...")


  val connection = Connection()
  val trialMessages = "Message 1" :: "Message 2" :: "Message 3" :: "Message 4" :: "Message 5" :: Nil
  val exchange = connection.publish(exchange = "streams-playground", routingKey = "")


  //if we want to be able to execute the flow from inside this object then we need to pass in a flowMaterializer
  //(if we remove the 'run' then we dont need the materializer)
  def produce(implicit flowMaterializer: ActorMaterializer) = {

    Source(trialMessages)
      .map(message => Message(ByteString(message), headers = Map("sent" -> Calendar.getInstance().getTime().toString)))
      .to(Sink.fromSubscriber(exchange))
      .run()
  }



}
