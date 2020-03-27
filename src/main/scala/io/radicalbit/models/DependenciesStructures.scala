package io.radicalbit.models

sealed case class DependenciesStructures(dependencies: Seq[sbt.ModuleID],
                                         resolvers: Seq[sbt.MavenRepository],
                                         credentials: Seq[sbt.Credentials])
