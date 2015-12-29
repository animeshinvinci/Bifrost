Getting Started with Scala, Akka, Reactive Streams and Grafana
===

With Reactive programming becoming more popular I decided to build a sample application to demonstrate some of the 
core concepts in action.  As I have recently started experimenting with Scala and the Akka framework, which has 
experimental support for Reactive Streams, I decided to try and build my sample application using Scala and 
Akka Reactive Streams.

While there are several good articles discussing what Reactive Programming is all about and even more tutorials on
building a simple reactive streams application using Scala, Akka and the Reactive Streams Framework, there aren't
many that cover all the necessary details from end to end.  I aim to change that.

In an interesting turn of events, as I was building this application Typesafe released akka-streams V2.0 
and V2.01 days later.  This new release of akka-streams introduced some significant changes to the framework and a 
nnumber of changes class names and the methods used to build reactive streams.

What We Will Cover
===

In this tutorial we will cover a range of related technologies:

1.  Reactive Programming and why its useful.
2.  Getting Started - Dependencies and Project Setup
3.  Akka Streams and how to build Graphs
    a.  Sources
    b.  Flows
    c.  Sinks
    d.  Putting it all Together
4.  Kamon and tracking what is happening in our application
5.  Examples of backpressure

The Basics - Overview
===

Let's start with something simple.  We want to create an Akka Stream that takes a series of text messages and outputs
them to the console.  This is nothing too special but gets the basics established.  We'll evolve this quickly to 
something more useful and better designed.


Essential Dependencies
===

We'll be using SBT as our build tool so we need to add a few dependencies to get things up and running.  The version 
numbers for akka-streams has recently bumped up to v2.0 which has rendered many of the existing tutorials out of date
since there have been some significant class renaming.

Our starting ```build.sbt``` has the following content:

```scala
name := "Bifrost"
    
version := "1.0"
    
scalaVersion := "2.11.7"
    
val akka            = "2.3.12"  
val akkaStream      = "2.0"
    
libraryDependencies ++= Seq (
 
    // -- Logging --
    ,"ch.qos.logback" % "logback-classic" % "1.1.2"
    ,"com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
   
    // -- Akka --
    ,"com.typesafe.akka" %% "akka-testkit" % akka % "test"
    ,"com.typesafe.akka" %% "akka-actor" % akka
    ,"com.typesafe.akka" %% "akka-slf4j" % akka
     
    // -- Akka Streams --
    ,"com.typesafe.akka" % "akka-stream-experimental_2.11" % akkaStream
   
    // -- Config --
    ,"com.typesafe" % "config" % "1.2.1"
)
```

The Basics - A Really Simple Stream
===

Let's take a look at some simple code that will do what we want and then we will examine it in detail, discussing some of the theory behind the implementation.

*SimpleStreams.scala*
```scala
package com.datinko.asgard.bifrost.tutorial

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

/**
 * An example of the simplest possible akka stream.
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
```   

Every stream has two essential components, without which it will not run:

![](http://i.imgur.com/PF84anG.png)

- A ```Source``` is something that has exactly one output.  It can produce a sequence of many items but they all follow one another, flowing from the Source.  We can use many things as a Source.  In our example above we use a simple List of strings.
- A ```Sink``` is something that has exactly one input and it is the final step in any stream.  In the example above we actually create a dummy Sink with the ```Sink.ignore``` call.  This ensures that the stream can run be we do not need to define the destination any further.

In the above example we use the ```.map(println(_))``` call to process each message that is emitted from the Sink and print it to the console.

Note that we have added the ```.run()``` call to the end of the stream definition so that the stream is automatically run when the ```printSimpleMessagesToConsole``` function is called.

This call to ```.run()``` is the reason that we must pass in the ```implicit materializer: ActorMaterializer``` as a parameter to the function.  The materializer is used to take the stream definition and turn it into a series of akka actors that can actually execute the stream.  If we did not make the call to ```.run()``` within this function the the materializer would not be needed.  We'll illustrate this in later examples.    

   
The Basics - Running our Simple Stream
===

To fire this stream up and make it do something, we create a simple application and make a call to our *SimpleStreams.printSimpleMessagesToConsole* method.

*Start.scala*

```scala
import akka.stream.{ActorMaterializer}

import com.datinko.asgard.bifrost.tutorial.SimpleStreams

/**
  * A really simple application to run our simple streams from.
  */
object Start extends App {

  implicit val materializer = ActorMaterializer()

  SimpleStreams.printSimpleMessagesToConsole(materializer)

}
```



 
 





