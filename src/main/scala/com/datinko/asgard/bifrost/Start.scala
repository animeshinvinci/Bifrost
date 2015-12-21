package com.datinko.asgard.bifrost

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer

/**
  * Created by Neil on 19/12/2015.
  */
object Start extends App {

  implicit val system = ActorSystem("bifrost")
  implicit val flowMaterializer = FlowMaterializer()

  RabbitMqConsumer.consume.run()



}
