package com.datinko.asgard.bifrost

import akka.actor.{DeadLetter, Props, ActorSystem}
import akka.stream.{ActorMaterializerSettings, ActorMaterializer}

import akka.stream.scaladsl.{Sink, Source}
import com.datinko.asgard.bifrost.actors.EchoActor
import com.datinko.asgard.bifrost.tutorial.SimpleStreams
import io.scalac.amqp.{Message, Connection}
import kamon.Kamon


/**
  * Created by Neil on 19/12/2015.
  */
object Start extends App {

  Kamon.start()

  implicit val system = ActorSystem("Bifrost")
  val deadLettersSubscriber = system.actorOf(Props[EchoActor], name = "dead-letters-subscriber")

  implicit val materializer = ActorMaterializer()

  val connection = Connection()
  val trialMessages = "Message 1" :: "Message 2" :: "Message 3" :: "Message 4" :: "Message 5" :: Nil
  val exchange = connection.publish(exchange = "streams-playground", routingKey = "")

  system.eventStream.subscribe(deadLettersSubscriber, classOf[DeadLetter])

  //Fire a set of messages at our Rabbit instance, wait a while and then consume them...
  //SimpleRabbitProducer.produceAsFastAsPossible(materializer)
  //SimpleRabbitProducer.produceAtControlledRate(materializer)
  ////Thread.sleep(2000)
  //SimpleRabbitConsumer.consume.run()

  //Scenarios.fastPublisherFastSubscriber(materializer).run()
  //Scenarios.fastPublisherSlowingSubscriber(materializer).run()
  //Scenarios.fastPublisherSlowingSubscriberWithDropBuffer(materializer).run()
  //Scenarios.fastPublisherSlowingSubscriberWithBackPressure(materializer).run()

  SimpleStreams.printSimpleMessagesToConsole(materializer)

}
