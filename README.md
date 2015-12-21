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
2. Using the RabbitMQ Admin UI, click on the 'Queues' tab at the top of the screen.
3. If it is not already present, create a queue with the name 'streams-playground'.
4. Once the queue has been created, click on the queue name to view its details and send messages manually using the UI.
5. To send a message to the queue simply enter some information in the 'Payload' box of the UI and click publish message.
6. If you have the application running then you will see the message appear in the console. 


