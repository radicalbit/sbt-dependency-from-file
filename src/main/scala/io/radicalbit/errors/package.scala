package io.radicalbit

package object errors {
  case class InvalidFieldException(message: String) extends RuntimeException(message) with DependencyFromJsonException

  case class ReducingResolverException(message: String)
      extends RuntimeException(message)
      with DependencyFromJsonException

  case class ReducingCredentialsException(message: String)
      extends RuntimeException(message)
      with DependencyFromJsonException
}
