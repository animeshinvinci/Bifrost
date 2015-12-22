package com.datinko.asgard.bifrost

import akka.actor.Actor
import akka.actor.Actor.Receive

/**
  * A simple actor that echo's the messages it recieves to the console.
  * (We subscribe an instance of this actor to the DeadLetter channel for debugging).
  */
class EchoActor extends Actor {

  override def receive = {

    case msg => println(s"New msg received: $msg")

  }
}
