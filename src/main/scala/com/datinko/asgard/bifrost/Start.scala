package com.datinko.asgard.bifrost

import akka.actor.{DeadLetter, Props, ActorSystem}
import akka.stream.ActorMaterializer

import akka.stream.scaladsl.{Sink, Source}
import io.scalac.amqp.{Message, Connection}

/**
  * Created by Neil on 19/12/2015.
  */
object Start extends App {

  implicit val system = ActorSystem("bifrost")
  val deadLettersSubscriber = system.actorOf(Props[EchoActor], name = "dead-letters-subscriber")

  implicit val materializer = ActorMaterializer()

  val connection = Connection()
  val trialMessages = "Message 1" :: "Message 2" :: "Message 3" :: "Message 4" :: "Message 5" :: Nil
  val exchange = connection.publish(exchange = "streams-playground", routingKey = "")

  system.eventStream.subscribe(deadLettersSubscriber, classOf[DeadLetter])

  //Fire a set of messages at our Rabbit instance, wait a while and then consume them...
  //SimpleRabbitProducer.produceAsFastAsPossible(materializer)
  SimpleRabbitProducer.produceAtControlledRate(materializer)
  Thread.sleep(2000)
  SimpleRabbitConsumer.consume.run()


}
