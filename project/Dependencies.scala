import sbt._

object Dependencies {

  object Versions {
    val catsEffect = "1.0.0"

    val kindProjector = "0.9.8"
    val betterMonadicFor = "0.3.0-M2"

    val scalaTest = "3.0.5"
  }

  object Libraries {
    lazy val catsEffect = "org.typelevel" %% "cats-effect"           % Versions.catsEffect
    lazy val scalaTest = "org.scalatest" %% "scalatest"              % Versions.scalaTest

    lazy val kindProjector = "org.spire-math"                        % "kind-projector" % Versions.kindProjector cross CrossVersion.binary
    lazy val betterMonadicFor = "com.olegpy" %% "better-monadic-for" % Versions.betterMonadicFor
  }
}
