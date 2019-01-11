import sbt._

object Dependencies {

  object scalatest {
    lazy val version = "3.0.1"
    lazy val namespace = "org.scalatest"
    lazy val core = namespace %% "scalatest" % version
  }

  object playjson {
    lazy val version = "2.6.10"
    lazy val namespace = "com.typesafe.play"
    lazy val core = namespace %% "play-json" % version
  }

  lazy val pluginDependencies = Seq(
    playjson.core,
    scalatest.core % Test
  )
}
