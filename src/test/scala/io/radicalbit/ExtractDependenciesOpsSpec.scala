package io.radicalbit

import java.io.File

import io.radicalbit.models.{Dependency, Resolver}
import org.scalatest.{Matchers, WordSpec}
import sbt.MavenRepository
import sbt._

object ExtractDependenciesOpsSpec {
  lazy val RightDependencies = Seq(
    Dependency(
      "io.radicalbit",
      "rtsae-operators-core",
      "1.2.0-SNAPSHOT",
      Some("2.11"),
      Resolver("Radicalbit Snapshots",
               "https://tools.radicalbit.io/artifactory/libs-snapshot-local/")
    ),
    Dependency(
      "io.radicalbit",
      "rtsae-operators-core-kafkastreams",
      "1.2.0-SNAPSHOT",
      Some("2.11"),
      Resolver("Radicalbit Snapshots",
               "https://tools.radicalbit.io/artifactory/libs-snapshot-local/")
    )
  )

  lazy val rightDependenciesAsModuleId: Seq[ModuleID] = Seq(
    "io.radicalbit" %% "rtsae-operators-core" % "1.2.0-SNAPSHOT",
    "io.radicalbit" %% "rtsae-operators-core-kafkastreams" % "1.2.0-SNAPSHOT"
  )
}
class ExtractDependenciesOpsSpec extends WordSpec with Matchers {
  import ExtractDependenciesOpsSpec._

  "ExtractDependenciesFromJson" should {
    "Extract dependencies from json file" in {
      val jsonFile =
        getClass.getClassLoader.getResource("json/dependencies.json").getFile()

      val extractedDependencies =
        ExtractDependenciesFromJsonPlugin.extractDependenciesTask(
          new File(jsonFile))
      info(s"Show extracted dependencies: $extractedDependencies")

      extractedDependencies.size shouldBe 2
      extractedDependencies shouldBe RightDependencies
    }

    "Extract dependencies from json file as ModuleId" in {
      val jsonFile =
        getClass.getClassLoader.getResource("json/dependencies.json").getFile()

      val extractedDependencies =
        ExtractDependenciesFromJsonPlugin.extractDependenciesTask(
          new File(jsonFile))
      info(s"Show extracted dependencies: $extractedDependencies")

      extractedDependencies.size shouldBe 2
      extractedDependencies.toModuleId.map(_.toString) shouldBe rightDependenciesAsModuleId
        .map(_.toString())
    }

    "Extract resolver from JsonFile" in {
      val jsonFile =
        getClass.getClassLoader.getResource("json/dependencies.json").getFile()

      val extractedResolver =
        ExtractDependenciesFromJsonPlugin.extractedResolversTask(new File(jsonFile))
      info(s"Show extracted dependencies: $extractedResolver")

      extractedResolver.size shouldBe 1
      extractedResolver shouldBe Seq(
        MavenRepository(
          "Radicalbit Snapshots",
          "https://tools.radicalbit.io/artifactory/libs-snapshot-local/")
      )
    }
  }

}
