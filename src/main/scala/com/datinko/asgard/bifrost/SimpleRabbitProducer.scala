package com.datinko.asgard.bifrost

import java.util.Calendar

import _root_.io.scalac.amqp.{Message, Connection}
import akka.stream.{ClosedShape, ActorMaterializer}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import akka.stream.scaladsl._
import scala.concurrent.duration._

case class Tick()

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
  //This simply sends our trial text content to Rabbit as AMQP messages as fast as it can.
  def produceAsFastAsPossibleAndSendToRabbit(implicit materializer: ActorMaterializer) = {

    Source(trialMessages)
      .map(message => Message(ByteString(message), headers = Map("sent" -> Calendar.getInstance().getTime().toString)))
      .to(Sink.fromSubscriber(exchange))
      .run()
  }

  //This sends our trial text content to Rabbit as AMQP messages at a controlled rate.
  def produceAtControlledRateAndSendToRabbit(implicit materializer: ActorMaterializer) = {

    //define our ticker to output a 'Tick' every 2 seconds - note Tick can be anything, could just be an Int, doesn't really matter for this demo
    val ticker = Source.tick(1.second, 2.seconds, Tick)

    //define some source message as a source (take the trial message contents and make AMQP messages for rabbit out of them
    val trialMessageSource = Source(trialMessages.map(message => Message(ByteString(message), headers = Map("sent" -> Calendar.getInstance().getTime().toString))))

    //define our final output - our RabbitMQ exchange
    val out = Sink.fromSubscriber(exchange)

    //define a stream to bring it all together..
    val zipStream = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>

      //import this so we can use the ~> syntax
      import GraphDSL.Implicits._

      //define a zip operaton that expects a tuple with a Tick and a Message in it..
      val zip = builder.add(Zip[Tick.type, Message])

      //define the inputs for the zip function - it wont fire until something arrives at both inputs, so we are essentially
      //throttling the output of this steam
      ticker ~> zip.in0
      trialMessageSource ~> zip.in1

      //send the output of our zip operation to a processing flow that just allows us to take the second element of each Tuple, in our case
      //this is the AMQP message, we dont care about the Tick, it was just for timing and we can throw it away.
      //route that to the 'out' Sink, the RabbitMQ exchange.
      zip.out ~> Flow[(Tick.type, Message)].map(_._2) ~> out

      ClosedShape
    })
    zipStream.run()
  }
}
