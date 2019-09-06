import Dependencies._

lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(
      organization := "com.fagossa",
      scalaVersion := "2.12.9",
      version := "0.1.0-SNAPSHOT"
    )),
    turbo := true, // increase test speed in tests
    name := "catless",
    licenses += ("MIT", new URL("https://opensource.org/licenses/MIT")),
    libraryDependencies ++= Seq(
      compilerPlugin(Libraries.kindProjector),
      compilerPlugin(Libraries.betterMonadicFor),
      Libraries.catsEffect,
      Libraries.scalaTest % Test
    )
  )
  .settings(partialUnification: _*)
  .settings(
    connectInput in run := true, // with this we can 'readLine'
    fork in run := true,
    scalafmtOnCompile := true,
    scalacOptions ++= commonScalacOptions
  )

lazy val commonScalacOptions = Seq(
  /*"-Xlog-implicits", */
  "-language:higherKinds",
  "-Xlint:type-parameter-shadow",
  "-Ywarn-unused:imports",
  "-Ywarn-dead-code",
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
  "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-value-discard"// Warn when non-Unit expression results are unused.
)

lazy val partialUnification = Seq(
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) if n >= 12 => Seq("-Ypartial-unification")
      case _ => Seq()
    }
  }
)
