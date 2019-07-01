name := "Webinar"

version := "0.1"

scalaVersion := "2.12.8"

dockerUpdateLatest := true

lazy val makeDockerVersion = taskKey[Seq[File]]("Creates a docker-version.sbt file we can find at runtime.")

lazy val root = (project in file("."))
  .settings(
    name := "root",
    libraryDependencies ++= Seq("org.apache.kafka" %% "kafka" % "2.0.0",
      "com.fasterxml.jackson.core" % "jackson-core" % "2.9.8",
      "com.fasterxml.jackson.core" % "jackson-annotations" % "2.9.8",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.8",
      "com.typesafe.play" %% "play-json" % "2.7.1",
      "com.typesafe.akka" %% "akka-http" % "10.1.7",
      "de.heikoseeberger" %% "akka-http-play-json" % "1.20.0",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7",
      "com.typesafe.akka" %% "akka-stream" % "2.5.19", "com.typesafe" % "config" % "1.2.1",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.8",
      "net.manub" %% "scalatest-embedded-kafka" % "0.14.0" % "test",
      "org.scalatest" %% "scalatest" % "3.0.5" % "test",
      "mysql" % "mysql-connector-java" % "5.1.12",
      "org.scalikejdbc" %% "scalikejdbc" % "3.3.2",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
      "ch.qos.logback" % "logback-classic" % "1.1.2",
      "org.slf4j" % "slf4j-api" % "1.7.10",
      "com.typesafe" % "config" % "1.3.3",
      "org.mockito" % "mockito-core" % "2.23.0",
      "org.mockito" %% "mockito-scala" % "1.1.4",
      "ch.megard" %% "akka-http-cors" % "0.4.0",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
      "com.typesafe" % "config" % "1.3.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
      "log4j" % "log4j" % "1.2.17",
      "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.19",
      "com.typesafe.akka" %% "akka-http-testkit" % "10.1.8"
    )
  )
  .settings(dockerSettings: _*)
  .enablePlugins(JavaAppPackaging)

lazy val dockerSettings = Seq(
  dockerBaseImage := "openjdk:8-jre-alpine",
  dockerRepository := Some(organization.value),
  dockerEntrypoint := Seq("bin/%s" format executableScriptName.value),
  dockerBuildOptions := Seq("--force-rm", "-t", dockerAlias.value.versioned),
  makeDockerVersion := makeDockerVersionTaskImpl.value
)

lazy val makeDockerVersionTaskImpl = Def.task {
  val propFile = file(".") / "target/docker-image.version"
  val content = dockerAlias.value.versioned
  println(s"Docker-version: $content")
  IO.write(propFile, content)
  Seq(propFile)
}

import com.typesafe.sbt.packager.docker._

dockerCommands += Cmd(
  "USER",
  "root"
)
dockerCommands += Cmd(
  "RUN",
  "apk add --update bash"
)
dockerCommands += Cmd(
  "RUN",
  "sed -e app_mainclass=com.knoldus.HttpMainService",
  s"${(defaultLinuxInstallLocation in Docker).value}/bin/${executableScriptName.value}"
)
