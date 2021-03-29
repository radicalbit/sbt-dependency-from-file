/*
 * Copyright 2019 Radicalbit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.radicalbit.extractor

import java.io.File

import cats.effect.IO
import io.radicalbit.models.{ Dependency, Resolver, Credentials => ConfigCredentials }
import org.scalatest.{ Matchers, WordSpec }
import sbt.{ MavenRepository, _ }

object DependencyLoaderSpec {
  lazy val RightDependencies = Seq(
    Dependency(
      "io.fake",
      "one-two-three",
      "1.2.0-SNAPSHOT",
      Some("2.11"),
      Resolver("Fake Snapshots", "https://fake.repo.io/artifactory/snapshot/", None)
    ),
    Dependency(
      "io.fake",
      "one-two-three-four",
      "1.2.0-SNAPSHOT",
      Some("2.11"),
      Resolver("Fake Snapshots", "https://fake.repo.io/artifactory/snapshot/", None)
    ),
    Dependency(
      "io.fake",
      "one-two-three-four",
      "1.2.0-SNAPSHOT",
      Some("2.11"),
      Resolver(
        "Fake Snapshots Two",
        "https://fake.repo.two.io/artifactory/snapshot/",
        Some(ConfigCredentials("Fake Artifcatory Realm", "fake.repo.two.io", "repoUser", "repoPwd"))
      )
    )
  )

  lazy val rightDependenciesAsModuleId: Seq[ModuleID] =
    RightDependencies.toModuleId
}

class DependencyLoaderSpec extends WordSpec with Matchers {
  import DependencyLoaderSpec._

  "ExtractDependenciesFromJson" should {

    "Dependencies Task" should {
      "Extract dependencies from json file" in {
        val jsonFile =
          getClass.getClassLoader.getResource("json/dependencies.json")

        val extractedDependencies = DependencyLoader[IO]
          .load(new File(jsonFile.toURI))
          .use(IO.pure)
          .unsafeRunSync()

        extractedDependencies.size shouldBe 3
        extractedDependencies shouldBe RightDependencies
      }

      "Extract dependencies from json file as ModuleId" in {
        val jsonFile =
          getClass.getClassLoader.getResource("json/dependencies.json")

        val extractedDependencies: Seq[String] = DependencyLoader[IO]
          .load(new File(jsonFile.toURI))
          .use(ExtractorBehaviour[IO].extractedModuleId.run)
          .map(_.map(_.toString()))
          .unsafeRunSync()

        extractedDependencies.size shouldBe 3
        extractedDependencies shouldBe rightDependenciesAsModuleId.map(_.toString)
      }

      "Return empty list from empty dependencies file" in {
        val jsonFile =
          getClass.getClassLoader.getResource("json/empty_dependencies.json")

        DependencyLoader[IO]
          .load(new File(jsonFile.toURI))
          .use(IO.pure)
          .unsafeRunSync() shouldBe List
          .empty[Dependency]
      }

      "Throw a runtime exception" in {
        val jsonFile = getClass.getClassLoader.getResource("json/dependencies_without_resolver.json")

        an[RuntimeException] should be thrownBy DependencyLoader[IO]
          .load(new File(jsonFile.toURI))
          .use(ExtractorBehaviour[IO].extractedResolvers.run)
          .unsafeRunSync()
      }
    }

    "Resolver Task" should {
      "Extract resolver from JsonFile" in {
        val jsonFile =
          getClass.getClassLoader.getResource("json/dependencies.json")

        val extractedResolver =
          DependencyLoader[IO]
            .load(new File(jsonFile.toURI))
            .use(ExtractorBehaviour[IO].extractedResolvers.run)
            .unsafeRunSync()

        extractedResolver.size shouldBe 2
        extractedResolver shouldBe Seq(
          MavenRepository("Fake Snapshots", "https://fake.repo.io/artifactory/snapshot/"),
          MavenRepository("Fake Snapshots Two", "https://fake.repo.two.io/artifactory/snapshot/")
        )
      }
    }
  }
}
