package com.datinko.asgard.bifrost

import akka.stream.ClosedShape
import akka.stream.javadsl.Broadcast
import akka.stream.scaladsl._

import scala.concurrent.duration.FiniteDuration

import scala.language.postfixOps

case class Tick()

/**
  * This is a work in progress and needs updating to Akka Streams 2
  */
class ThrottledSource {



//  def produceStream(delay: FiniteDuration, interval: FiniteDuration, numberOfMessages: Int, name: String) : Unit = {
//
//    val graph = RunnableGraph.fromGraph(GraphDSL.create() {
//
//      implicit  builder: GraphDSL.Builder[Unit] =>
//        import GraphDSL.Implicits._
//
//        val tickSource = Source.tick(delay, interval, Tick())
//        val rangeSource = Source(1 to numberOfMessages)
//
//        // we use zip to throttle the stream
//        val zip = builder.add(Zip[Tick, Int]())
//        val unzip = builder.add(Flow[(Tick, Int)].map(_._2))
//        val output = Sink.ignore
//
//        // setup the message flow
//        tickSource ~> zip.in0
//        rangeSource ~> zip.in1
//        zip.out ~> unzip ~> output //~> sendMap
//
//
//        ClosedShape
//    })

//  }




}


