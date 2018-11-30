import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.7",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "catless",
    libraryDependencies ++= Seq(
      "org.typelevel"  %% "cats-effect"   % "1.0.0",
      "org.tpolecat"   %% "doobie-core"   % "0.6.0",
      "org.tpolecat"   %% "doobie-h2"     % "0.6.0",
      scalaTest        % Test
    )
  )
  .settings(Scalariform.settings)

scalacOptions += "-Ypartial-unification"