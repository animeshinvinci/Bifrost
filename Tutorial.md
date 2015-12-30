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
- A ```Sink``` is something that has exactly one input and it is the final step in any stream.  In the example above we actually create a dummy Sink with the ```Sink.ignore``` call.  This ensures that the stream can run but we do not need to define the destination any further.

In the above example we use the ```.map(println(_))``` call to process each message that is emitted from the Sink and print it to the console.

Note that we have added the ```.run()``` call to the end of the stream definition so that the stream is automatically run when the ```printSimpleMessagesToConsole``` function is called.

This call to ```.run()``` is the reason that we must pass in the ```implicit materializer: ActorMaterializer``` as a parameter to the function.  The materializer is used to take the stream definition and turn it into a series of akka actors that can actually execute the stream.  If we did not make the call to ```.run()``` within this function the the materializer would not be needed.  We'll illustrate this in later examples.    

   
The Basics - Running our Simple Stream
===

To fire this stream up and make it do something, we create a simple application and make a call to our ```SimpleStreams.printSimpleMessagesToConsole``` method.

*Start.scala*

```scala
package com.datinko.asgard.bifrost
import akka.stream.{ActorMaterializer}

import com.datinko.asgard.bifrost.tutorial.SimpleStreams

/**
  * A really simple application to run our simple streams from.
  */
object Start extends App {

  implicit val system = ActorSystem("Bifrost")
  implicit val materializer = ActorMaterializer()

  SimpleStreams.printSimpleMessagesToConsole(materializer)

}
```

There are two supporting elements that we need in our application to get our stream working:

- An ```ActorSystem``` is an Akka actor system.  As Akka Streams are, somewhat obviously, based on akka, we need to have an actor system up and running for the streams to work.  We have done the simplest possible to create an instance of an akka system with a name of our choosing.  Making this ```implicit``` means that it can be picked up an used in other code whenever it is needed.
- The ```Materializer``` as discussed above provides the processing power to turn a stream definition into something that can actually be executed with a series of akka actors.  Again, we have made this ```implicit``` so that it can be picked up and used in other code whenever it is needed.  The main user of this materializer is our ```SimpleStreams``` object.

When you execute this application, either from within your IDE or from the command line using ```sbt run``` you should see output like:

```
Message 1
Message 2
Message 3
Message 4
Message 5
```

Fairly uninspiring stuff but we now have a working system with all the plumbing we need to get streams up and running.  Now we can move on and make some more useful and better designed streams.

Stream Graphs - Overview
===

In our simple early example of a stream, we did everything in one function.  This is rarely how we would do things in a production environment.  Often we would want to split up the parts of our stream into components that we can reuse.  Once we have these components we will probably want to wire those components together in a range of ways to get the stream we want.  Stream Graphs give us a way to do this.

Splitting up our Sources, Flows and Sinks
===

Although we said that streams has two *essential* components for them to work, we failed to mention that there is one other type of component that makes it easier to decompose our streams into reusable chunks of functionality.

![](http://i.imgur.com/nkvRPX3.png)

- A ```Flow``` is something that has exactly one input and exactly one output.  A flow is often used to modify the messages that pass through the stream.  A stream can contain many flows and there are a range of common operations that can be used within a flow.  You can also implement your own operations within a flow.

Creating a Reusable Source
===

One of the main objectives we would like to achieve in this tutorial is to create a number of streams that we can use to experiment with *backpressure* and monitor to observe its effects.  To do that we will need a ```Source``` object that we can reuse in many different scenarios with a range of different message production rates.  


Say Hello to the ThrottledProducer
===

The first step to building such a system is to create a ```Source``` that produces the data we want at the rate we want.  We then want to be able to wire that source into a range of other components that will consume the messages that are produced.  We will call this source the ```ThrottledProducer```.  Bear with, there may be a lot of typing before we see results.

The ThrottledProducer - Overview
===
In order to get a ```Source``` produce message at a rate that we can control, we need to create a Stream Graph and wrap it up as a ```Source``` that can be reused in another Stream Graph.  The ```ThrottledProducer``` has the following components within it.

![](http://i.imgur.com/tUey1NP.png)

- The ```Ticker``` is a built is akka-stream component that produces simple messages at a defined rate.  This is almost perfect for our needs, except that we want to control the content of the messages that our Source produces.
- The ```NumberGenerator``` is simple a Scala ```Range``` that contains the number of messages we want.  In our case, the content of each message is an actual number.  Message 1 contains the number 1, message 2 contains the number 2 and so on.
- The ```Zip``` operation is a build in akka-stream component and key to controlling the rate at which we produce messages inside our source.  The ```Zip``` operation waits until it has a value at both inputs before it produces an output.  The output takes the form of a tuple containing both inputs.  In our example our ```NumberGenerator``` produces all the messages we want to output almost immediately.  The Ticker produces ```Tick``` messages at the controlled rate we specify.  The messages from the ```NumberGenerator``` wait at the input of the ```Zip``` until a ```Tick``` arrives at its other input.  When this happens the ```Zip``` outputs a Tuple of form ```[Tick, Int]``` and sends it on to its output.
- The ```ExtractFlow``` is a simple flow operation that extracts the ```Int``` element of the ```Zip``` output and passes it on.  It discards the ```Tick``` as it is not needed.  We only needed the ```Tick``` to control the rate at which messages were produced.

The ThrottledProducer - Code
===
Enough theory, lets take a look at the code.

*ThrottledProducer.scala*

```scala
package com.datinko.asgard.bifrost

import akka.stream.{SourceShape, ActorMaterializer}
import akka.stream.scaladsl.{Flow, Zip, GraphDSL, Source}

import scala.concurrent.duration.FiniteDuration

/**
 * A really, really simple case class to represent a Tick of our ticker source.
 * (This could be anything really, nothing special here)
 */
case class Tick()

/**
 * An Akka Streams Source helper that produces messages at a defined rate.
 */
object ThrottledProducer {

  def produceThrottled(implicit materializer: ActorMaterializer, initialDelay: FiniteDuration, interval: FiniteDuration, numberOfMessages: Int, name: String) = {

    val ticker = Source.tick(initialDelay, interval, Tick)
    val numbers = 1 to numberOfMessages
    val rangeMessageSource = Source(numbers)

    //define a stream to bring it all together..
    val throttledStream = Source.fromGraph(GraphDSL.create() { implicit builder =>

      //define a zip operation that expects a tuple with a Tick and an Int in it..
      //(Note that the operations must be added to the builder before they can be used)
      val zip = builder.add(Zip[Tick.type, Int])

      //create a flow to extract the second element in the tuple (our message - we dont need the tick part after this stage)
      val messageExtractorFlow = builder.add(Flow[(Tick.type, Int)].map(_._2))

	  //import this so we can use the ~> syntax
      import GraphDSL.Implicits._

      //define the inputs for the zip function - it wont fire until something arrives at both inputs, so we are essentially
      //throttling the output of this steam
      ticker ~> zip.in0
      rangeMessageSource ~> zip.in1

      //send the output of our zip operation to a processing messageExtractorFlow that just allows us to take the second element of each Tuple, in our case
      //this is the Int, we dont care about the Tick, it was just for timing and we can throw it away.
      //route that to the 'out' Sink so that the next graph component we wire the source into can get to it
      zip.out ~> messageExtractorFlow

      SourceShape(messageExtractorFlow.out)
    })
    throttledStream
  }
}
```

The whole purpose of this function is to create a ```Source``` stream that we have called ```throttledStream``` that can included as part of another Stream Graph.

We start by defining our two Source objects, the ```ticker``` and the collection of number messages.  The ```Source``` object has handy constructors that allow us to pass in many different types of collections that are automatically turned into valid sources.  We'll use these two sources a little later to feed into our ```Zip``` operation.

The definition of the Stream Graph is started with:

```val throttledStream = Source.fromGraph(.......)```

We signal our intention to create a Graph from scratch using:

```GraphDSL.create() { implicit builder =>.......}```

All GraphDSL operations need reference to a ```builder``` object which acts as a context for the Stream Graph.  Sources, sinks, flows and opearations are registered with the builder.  Once that is done they can be wired together into a stream definition. 

The next steps are to create the operations we want within our graph.  First we build define the ```Zip``` operation that expects to receive inputs of type ```Tick``` and ```Int``` and therefore produce a Tuple with the same type of elements.

```val zip = builder.add(Zip[Tick.type, Int])```

The next operation to add to the builder is the message extractor flow which ensures that we only take the second element in each tuple and discard the ```Tick``` element that we no longer need.  (The map ```Flow``` defines the type of objects it expects to receive while the ```map``` operation simply tells the flow to extract and pass on the second element of each message it receives).

```val messageExtractorFlow = builder.add(Flow[(Tick.type, Int)].map(_._2))```

ThrottledProducer - Code - Building the Graph
===
Once we have defined all the sources, flows and operations that will be used in the graph, we need to wire those elements together to actually form the graph.

To make this easier the GraphDSL provides a number of implicits we can use.  To access these we import them using:

```import GraphDSL.Implicits._```

The most obvious benefit of this is that it allows us to use the ```~>``` operator to wire components together in the graph, like so:

```scala
ticker ~> zip.in0

rangeMessageSource ~> zip.in1

zip.out ~> messageExtractorFlow
```

(Note that this illustrates that the ```Zip``` has a number inputs and a single output).

The final element inside the Graph definition is to indicate what kind of Graph component we are creating (Source, Sink or Flow).  

```
SourceShape(messageExtractorFlow.out)
```

The ```SourceShape``` call indicates that this is a source component and we must tell the Graph which of its internal operations supplies the messages that will be exposed to consumers of this source.

(Note that there are ```SinkShape``` and ```FlowShape``` elements to support making Sinks and Flows).

Once we have build the graph for the ```throttledSource``` we return a reference to the function so that it can be used when building another Graph.

Using the ThrottledProducer in a Graph
===
Now that we have built our ```ThrottledProducer``` we want to use it for something.  Believe it or not, the cleanest way of showing the ```ThrottledProducer``` in action is to use it in another simple Graph and then run that graph.

If we add a new function to the ```SimpleStreams``` object we created earlier we get the following.  This uses a lot of the concepts we've just covered in the ```ThrottledProducer``` to build a stream graph, expect that this builds a runnable (also known as a 'closed') graph that is complete with a ```Source``` and a ```Sink```.

*SimpleStreams.scala*
```scala
def throttledProducerToConsole(implicit materializer: ActorMaterializer) = {

    val theGraph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[Unit] =>

      import GraphDSL.Implicits._

      val source = builder.add(ThrottledProducer.produceThrottled(materializer, 1 second, 20 milliseconds, 20000, "fastProducer"))
      val printFlow = builder.add(Flow[(Message)].map{println(_)})

      val sink = builder.add(Sink.ignore)

      source ~> printFlow ~> sink

      ClosedShape
    })
    theGraph
  }
```

The differences to note about this function is that it creates a ```RunnableGraph``` which indicates it should have a ```Source``` and a ```Sink```.  This is matched by the use of ```ClosedShape``` at the end of the function rather than than the ```SourceShape``` we used in the ```ThrottledProducer```.

As we did previously, we define the components to be used in the graph.  In this case we have our ```ThrottledProducer``` that is set to produce a message every 20 milliseconds after an initial delay of 1 second and it will produce 20000 messages.

To make it a little easier to see if anything is happening we have created a simple flow that prints every message it sees.

This is all capped off by the ```Sink.ignore``` that we have seen earlier.  This simply gives the graph the sink it must have to be considered valid but this sink simply discards all messages it receives.

To run this graph we simply add the following to our ```Start.scala``` object.

```scala
SimpleStreams.throttledProducerToConsole.run()
```

TODO: Example of output