import Dependencies._

ThisBuild / scalaVersion     := "0.24.0-RC1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.luxaritas"
ThisBuild / organizationName := "luxaritas"

scalacOptions += "-deprecation"
scalacOptions ++= { if (isDotty.value) Seq("-language:Scala2Compat") else Nil }

lazy val root = (project in file("."))
  .aggregate(scalurak)
  .dependsOn(scalurak)
  .settings(
    name := "ScaLUS",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += akkaActors,
    libraryDependencies := libraryDependencies.value.map(_.withDottyCompat(scalaVersion.value))
  )

lazy val scalurak = (project in file("scalurak"))
  .settings(
    name := "Scala Raknet",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += akkaActors,
    libraryDependencies := libraryDependencies.value.map(_.withDottyCompat(scalaVersion.value))
  )
