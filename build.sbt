import Dependencies._

lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(
      organization := "com.fagossa",
      scalaVersion := "2.12.9",
      version := "0.1.0-SNAPSHOT"
    )),
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
  "-Ywarn-dead-code"
)

lazy val partialUnification = Seq(
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) if n >= 12 => Seq("-Ypartial-unification")
      case _ => Seq()
    }
  }
)
