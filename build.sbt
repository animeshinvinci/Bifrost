name := "Bifrost"

version := "1.0"

scalaVersion := "2.11.7"

val akka = "2.3.8"

/* dependencies */
libraryDependencies ++= Seq (
  "com.github.nscala-time" %% "nscala-time" % "1.4.0"
  // -- testing --
  , "org.scalatest" %% "scalatest" % "2.2.2" % "test"
  // -- Logging --
  ,"ch.qos.logback" % "logback-classic" % "1.1.2"
  ,"com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
  // -- Akka --
  ,"com.typesafe.akka" %% "akka-testkit" % akka % "test"
  ,"com.typesafe.akka" %% "akka-actor" % akka
  ,"com.typesafe.akka" %% "akka-slf4j" % akka
  ,"com.typesafe.akka" %% "akka-http-experimental" % "1.0-M2"
  // -- json --
  ,"com.typesafe.play" %% "play-json" % "2.4.0-M2"
  // -- config --
  ,"com.typesafe" % "config" % "1.2.1"
  ,"io.scalac" %% "reactive-rabbit" % "0.2.2"
  ,"de.heikoseeberger" %% "akka-sse" % "0.2.1"
)

/* you may need these repos */
resolvers ++= Seq(
  // Resolver.sonatypeRepo("snapshots")
  // Resolver.typesafeRepo("releases")
  //"spray repo" at "http://repo.spray.io"
  "hseeberger at bintray" at "http://dl.bintray.com/hseeberger/maven"
)