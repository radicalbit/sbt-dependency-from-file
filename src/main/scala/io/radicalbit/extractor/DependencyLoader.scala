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

import cats.effect._
import cats.implicits._
import io.radicalbit.errors._
import io.radicalbit.models._
import play.api.libs.json.Json

import java.io.File

sealed trait DependencyLoader[F[_]] {
  def load(file: File): Resource[F, Seq[Dependency]]
}

object DependencyLoader {
  def apply[F[_]: Sync](implicit extractor: DependencyLoader[F]): DependencyLoader[F] = instanceMaker[F]

  implicit def instanceMaker[F[_]](implicit S: Sync[F]): DependencyLoader[F] =
    new DependencyLoader[F] {
      def load(file: File): Resource[F, Seq[Dependency]] =
        Resource
          .fromAutoCloseable(S.delay(scala.io.Source.fromFile(file)))
          .evalMap { buffer =>
            S.fromEither {
              Json
                .parse(buffer.getLines().mkString(""))
                .validate[Seq[Dependency]]
                .asEither
                .leftMap(errors => InvalidFieldException(errors.mkString(", ")))
            }
          }
    }

}
