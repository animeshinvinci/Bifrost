Bifröst
====

In Norse mythology, Bifröst is a burning rainbow bridge that reaches between Midgard and Asgard, the realm of the gods 
and the realm of humans.

The intentions is to build a [Reactive Streams](http://www.reactive-streams.org) bridge between [RabbitMQ](https://www.rabbitmq.com/) and 
[Apache Kafka](http://kafka.apache.org/)

The intention is to provide a reactive consumer from RabbitMQ that can then push messages to a Kafka broker and act as a bridge between
the two technologies.

Getting Started
====
This assumes that you have RabbitMQ installed locally and have the management UI plugin installed and running.

1. Ensure that a local instance of RabbitMQ is running and use your browser to navigate to [The RabbitMQ UI](http://localhost:15672/#/).
2. Using the RabbitMQ Admin UI, click on the 'Exchanges' tab at the top of the screen.
3. If it is not already present, create an exchange with the name 'streams-playground'.
4. Using the RabbitMQ Admin UI, click on the 'Queues' tab at the top of the screen.
5. If it is not already present, create a queue with the name 'streams-playground'.
6. Go back to the 'streams-playground' exchange entry.
7. Use the UI to add a binding to the exchange to bind it to the 'streams-playground' queue.  This ensures that the 
   messages that are sent to the exchange are sent to the queue.
8. Once the exchange has been created and bound to the queue, click on the exchange name to view its details and send 
   messages manually using the UI.
9. To send a message to the exchange simply enter some information in the 'Payload' box of the UI and click publish 
   message.
6. Run 'Start.scala' from IntelliJ (or sbt run from the console).
7. You will see the message headers with their bodies appear in the console as they are sent the exchange and receieved 
   by the queue.
   
Supporting Sources
===

Bits of this implementation have been borrowed from the following interesting souces:

https://github.com/ScalaConsultants/reactive-rabbit
http://blog.michaelhamrah.com/2015/01/a-gentle-introduction-to-akka-streams/
http://blog.michaelhamrah.com/2015/01/adding-http-server-side-events-to-akka-streams/
https://github.com/jczuchnowski/rabbitmq-akka-stream/blob/master/src/main/scala/io/scalac/rabbit/ConsumerApp.scala

http://www.smartjava.org/content/visualizing-back-pressure-and-reactive-streams-akka-streams-statsd-grafana-and-influxdb
(Go here next - some nice classes for simulating slow consumers etc)


This project has been updated to Akka Stream 2.0.  This means some changes and Reactive Rabbit may not play nicely.

See [Akka Streams 2 Docs](http://doc.akka.io/docs/akka-stream-and-http-experimental/2.0/scala.html) for more details.


Phase Two - Monitoring
===

Being able to create reactive components that are able to create and respond to backpressure is great.  However, we need
to be able to see this reactive backpressure in action.  To achieve this we'll add some monitoring to our application 
using Kamon.  

Kamon allows us to see some useful performance metrics about Akka systems.  Kamon simply gathers these metrics and then
sends them to other components for aggregation and presentation.  One of the bets tools for aggregating these metrics
is the StatsD package.  For presentation we'll use the Graphite dashboarding system.  To get these componenets up and
running toether with all the supporting components can take some time.  Thankfully a clever soul has already made a 
docker image of all the things we need. To get your mitts on it and get it up and running, use:

docker run -p 80:80 -p 8125:8125/udp -p 8126:8126 -p 8083:8083 -p 8086:8086 -p 8084:8084 --name kamon-grafana-dashboard muuki88/grafana_graphite:latest

Once docker has done its thing you can see the dashboard at 'localhost:80' (if you are using on windows or mac using
Docker Toolbox remember that your default IP will not be localhost or 127.0.0.1 - its likely to be http://192.168.99.100).

Fire up the application (you must use 'sbt run' and it will start to collect stats from the running application and send them
to StatsD, from where Grafana can get them.

If you fire up grafana (http://192.168.99.100/#/dashboard/file/default.json) then click on the title of the chart 
that is shown at the bottom of the page, you can click 'Edit' from the menu that appears.

If you take a look at the query that is shown at the bottom of the page that appears you will be able to navigate
through the list of recorded metrics.

Navigate down to:
 'stats | timers | Bifrost | <Machine Name> | akka-actor | kamon_user_a | time-in-mailbox | mean

and enjoy a pretty graph of the mean time that messages stay in the mail box of the producer.

** NEED TO INSERT THE NEW DEPENDENCIES HERE AS WELL AS THE PLUGINS AND THE ASSOCIATED CONFIG FOR ASPECTJ **


Next - 

1. Add some meaningful metrics.
2. Create fast/slow producers and consumers.
3. Make graphs of their performance to illustate it all in motion.
4. Clean up the docs.
5. Make a presentation!


