package com.datinko.asgard.bifrost.tutorial

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

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
}
