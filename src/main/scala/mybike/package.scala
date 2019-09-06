package object mybike {
  type ErrorOr[T] = Either[String, T]
}
