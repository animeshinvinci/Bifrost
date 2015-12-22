name := "Bifrost"

version := "1.0"

scalaVersion := "2.11.7"

//val akka            = "2.4.1"
val akka            = "2.3.12"  //need this for reactive rabbit
val reactiveRabbit  = "1.0.3"
val akkaStream = "2.0"

/* dependencies */
libraryDependencies ++= Seq (
  "com.github.nscala-time" %% "nscala-time" % "1.4.0"

  // -- Testing --
  , "org.scalatest" %% "scalatest" % "2.2.2" % "test"

  // -- Logging --
  ,"ch.qos.logback" % "logback-classic" % "1.1.2"
  ,"com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

  // -- Akka --
  ,"com.typesafe.akka" %% "akka-testkit" % akka % "test"
  ,"com.typesafe.akka" %% "akka-actor" % akka
  ,"com.typesafe.akka" %% "akka-slf4j" % akka
  //,"com.typesafe.akka" %% "akka-http-experimental" % "1.0-M2"

  // --akka-streams
  ,"com.typesafe.akka" % "akka-stream-experimental_2.11" % akkaStream

  // -- json --
  //,"com.typesafe.play" %% "play-json" % "2.4.0-M2"

  // -- Reactive Rabbit --
  ,"io.scalac" %% "reactive-rabbit" % reactiveRabbit

  // -- config --
  ,"com.typesafe" % "config" % "1.2.1"

  ,"de.heikoseeberger" %% "akka-sse" % "0.2.1"
)

/* you may need these repos */
resolvers ++= Seq(
  // Resolver.sonatypeRepo("snapshots")
  // Resolver.typesafeRepo("releases")
  //"spray repo" at "http://repo.spray.io"
  "hseeberger at bintray" at "http://dl.bintray.com/hseeberger/maven"
)