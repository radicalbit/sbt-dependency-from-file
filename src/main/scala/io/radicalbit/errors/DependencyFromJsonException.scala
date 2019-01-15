package io.radicalbit.errors

trait DependencyFromJsonException { self: Throwable =>

  def message: String

  def throwEx = throw self

}
