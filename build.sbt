import Dependencies._

lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.7",
      version := "0.1.0-SNAPSHOT"
    )),
    name := "catless",
    libraryDependencies ++= Seq(
      catsEffect,
      scalaTest % Test
    )
  )
  .settings(
    fork in run := true,
    scalafmtOnCompile := true,
    scalacOptions ++= Seq(
      /*"-Xlog-implicits", */
      "-Ypartial-unification",
      "-language:higherKinds",
      "-Xlint:type-parameter-shadow",
      "-Ywarn-unused:imports",
      "-Ywarn-dead-code"
    )
  )
