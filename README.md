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

