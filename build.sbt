name := "JustInServer"

version := "1.0"

scalaVersion := "2.10.4"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies += "io.spray" % "spray-can" % "1.3.1"

libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.1"

libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.1"

libraryDependencies += "org.scalactic" % "scalactic_2.10" % "2.2.2"

libraryDependencies += "postgresql" % "postgresql" % "9.1-901-1.jdbc4"

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.10.4"

libraryDependencies += "org.scala-lang" % "scala-library" % "2.10.4"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.3.5"

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.10.4"

//libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.3.7"
//
//libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.11.4"
//
//libraryDependencies += "org.scala-lang" % "scala-library" % "2.11.4"
//
//libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.3.7"
//
//libraryDependencies += "com.typesafe.akka" % "akka-testkit_2.10" % "2.3.7"

//libraryDependencies += "org.scalatest" % "scalatest_2.10" % "3.0.0-SNAP2"

//libraryDependencies += "log4j" % "log4j" % "1.2.17"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.0" % "test"

