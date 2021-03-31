package io.radicalbit

import io.radicalbit.models.DependenciesStructures
import sbt.settingKey

import java.io.File

trait PluginKeys {
  lazy val dependenciesJsonPath = settingKey[File]("Dependencies file path")
  lazy val dependenciesFromJson = settingKey[DependenciesStructures]("Extracted information")
}
