package io.radicalbit.extractor

import java.io.File

import cats.effect.IO
import io.radicalbit.models.{
  Dependency,
  Resolver,
  Credentials => ConfigCredentials
}
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
      Resolver("Fake Snapshots",
               "https://fake.repo.io/artifactory/snapshot/",
               None)
    ),
    Dependency(
      "io.fake",
      "one-two-three-four",
      "1.2.0-SNAPSHOT",
      Some("2.11"),
      Resolver("Fake Snapshots",
               "https://fake.repo.io/artifactory/snapshot/",
               None)
    ),
    Dependency(
      "io.fake",
      "one-two-three-four",
      "1.2.0-SNAPSHOT",
      Some("2.11"),
      Resolver(
        "Fake Snapshots Two",
        "https://fake.repo.two.io/artifactory/snapshot/",
        Some(
          ConfigCredentials("Fake Artifcatory Realm",
                            "fake.repo.two.io",
                            "repoUser",
                            "repoPwd"))
      )
    )
  )

  lazy val rightDependenciesAsModuleId: Seq[ModuleID] =
    RightDependencies.toModuleId
}

class ExtractDependenciesOpsSpec extends WordSpec with Matchers {
  import ExtractDependenciesOpsSpec._
  implicit val extractor: Extractor[IO] = Extractor.dependenciesExtractor[IO]

  "ExtractDependenciesFromJson" should {

    "Dependencies Task" should {
      "Extract dependencies from json file" in {
        val jsonFile =
          getClass.getClassLoader.getResource("json/dependencies.json")

        val extractedDependencies = extractor
          .load(new File(jsonFile.toURI))
          .use(IO.pure)
          .unsafeRunSync()

        extractedDependencies.size shouldBe 3
        extractedDependencies shouldBe RightDependencies
      }

      "Extract dependencies from json file as ModuleId" in {
        val jsonFile =
          getClass.getClassLoader.getResource("json/dependencies.json")

        val extractedDependencies: Seq[String] = extractor
          .load(new File(jsonFile.toURI))
          .use(extractor.extractedModuleId.run)
          .map(_.map(_.toString()))
          .unsafeRunSync()

        extractedDependencies.size shouldBe 3
        extractedDependencies shouldBe rightDependenciesAsModuleId.map(
          _.toString)
      }

      "Return empty list from empty dependencies file" in {
        val jsonFile =
          getClass.getClassLoader.getResource("json/empty_dependencies.json")

        extractor
          .load(new File(jsonFile.toURI))
          .use(IO.pure)
          .unsafeRunSync() shouldBe List
          .empty[Dependency]
      }

      "Throw a runtime exception" in {
        val jsonFile = getClass.getClassLoader.getResource(
          "json/dependencies_without_resolver.json")

        an[RuntimeException] should be thrownBy extractor
          .load(new File(jsonFile.toURI))
          .use(extractor.extractedResolvers.run)
          .unsafeRunSync()
      }
    }

    "Resolver Task" should {
      "Extract resolver from JsonFile" in {
        val jsonFile =
          getClass.getClassLoader.getResource("json/dependencies.json")

        val extractedResolver =
          extractor
            .load(new File(jsonFile.toURI))
            .use(extractor.extractedResolvers.run)
            .unsafeRunSync()

        extractedResolver.size shouldBe 2
        extractedResolver shouldBe Seq(
          MavenRepository("Fake Snapshots",
                          "https://fake.repo.io/artifactory/snapshot/"),
          MavenRepository("Fake Snapshots Two",
                          "https://fake.repo.two.io/artifactory/snapshot/")
        )
      }
    }
  }
}
