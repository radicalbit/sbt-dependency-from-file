package io.radicalbit

import java.io.File

import io.radicalbit.models.{Dependency, Resolver}
import org.scalatest.{Matchers, WordSpec}
import sbt.MavenRepository
import sbt._

object ExtractDependenciesOpsSpec {
  lazy val RightDependencies = Seq(
    Dependency(
      "io.fake",
      "one-two-three",
      "1.2.0-SNAPSHOT",
      Some("2.11"),
      Resolver("Fake Snapshots", "https://fake.repo.io/artifactory/snapshot/")
    ),
    Dependency(
      "io.fake",
      "one-two-three-four",
      "1.2.0-SNAPSHOT",
      Some("2.11"),
      Resolver("Fake Snapshots", "https://fake.repo.io/artifactory/snapshot/")
    )
  )

  lazy val rightDependenciesAsModuleId: Seq[ModuleID] = Seq(
    "io.fake" %% "one-two-three" % "1.2.0-SNAPSHOT",
    "io.fake" %% "one-two-three-four" % "1.2.0-SNAPSHOT"
  )
}
class ExtractDependenciesOpsSpec extends WordSpec with Matchers {
  import ExtractDependenciesOpsSpec._

  "ExtractDependenciesFromJson" should {

    "Dependencies Task" should {
      "Extract dependencies from json file" in {
        val jsonFile =
          getClass.getClassLoader.getResource("json/dependencies.json")

        val extractedDependencies =
          ExtractDependenciesFromJsonPlugin.extractDependenciesTask(
            new File(jsonFile.toURI))

        extractedDependencies.size shouldBe 2
        extractedDependencies shouldBe RightDependencies
      }

      "Extract dependencies from json file as ModuleId" in {
        val jsonFile =
          getClass.getClassLoader.getResource("json/dependencies.json")

        val extractedDependencies =
          ExtractDependenciesFromJsonPlugin.extractDependenciesTask(
            new File(jsonFile.toURI))

        extractedDependencies.size shouldBe 2
        extractedDependencies.toModuleId.map(_.toString) shouldBe rightDependenciesAsModuleId
          .map(_.toString)
      }

      "Return empty list from empty dependencies file" in {
        val jsonFile =
          getClass.getClassLoader.getResource("json/empty_dependencies.json")

        ExtractDependenciesFromJsonPlugin
          .extractedResolversTask(new File(jsonFile.toURI)) shouldBe List
          .empty[Dependency]
      }

      "Throw a runtime exception" in {
        val jsonFile = getClass.getClassLoader.getResource(
          "json/dependencies_without_resolver.json")

        an[RuntimeException] should be thrownBy ExtractDependenciesFromJsonPlugin
          .extractedResolversTask(new File(jsonFile.toURI))
      }
    }

    "Resolver Task" should {
      "Extract resolver from JsonFile" in {
        val jsonFile =
          getClass.getClassLoader.getResource("json/dependencies.json")

        val extractedResolver =
          ExtractDependenciesFromJsonPlugin.extractedResolversTask(
            new File(jsonFile.toURI))

        extractedResolver.size shouldBe 1
        extractedResolver shouldBe Seq(
          MavenRepository("Fake Snapshots",
                          "https://fake.repo.io/artifactory/snapshot/")
        )
      }
    }
  }
}
