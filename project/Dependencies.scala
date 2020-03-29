import sbt._

object Dependencies {

  object scalatest {
    lazy val version = "3.0.6"
    lazy val namespace = "org.scalatest"
    lazy val core = namespace %% "scalatest" % version
  }

  object playjson {
    lazy val version = "2.6.13"
    lazy val namespace = "com.typesafe.play"
    lazy val core = namespace %% "play-json" % version
  }

  object catsEffect {
    lazy val version = "1.3.0"
    lazy val namespace = "org.typelevel"
    lazy val core = namespace %% "cats-effect" % version
  }

  lazy val pluginDependencies = Seq(
    playjson.core,
    catsEffect.core,
    scalatest.core % Test
  )
}
