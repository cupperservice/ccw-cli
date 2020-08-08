import Dependencies._

enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "cupper"
ThisBuild / organizationName := "cupper"

val awsSdkVersion = "2.715.0"
val awsSdkScalajsFacadeVersion = s"0.30.0-v${awsSdkVersion}"

lazy val root = (project in file("."))
  .settings(
    name := "ccw",
    libraryDependencies ++= Seq(
      "net.exoego" %%% "scala-js-nodejs-v12" % "0.12.0",
      "net.exoego" %%% "aws-sdk-scalajs-facade-ec2" % awsSdkScalajsFacadeVersion,
      "net.exoego" %%% "aws-sdk-scalajs-facade-cloudwatch" % awsSdkScalajsFacadeVersion,
      scalaTest % Test,
    ),
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
    scalaJSUseMainModuleInitializer := true,
    mainClass := Some("cupper.Main"),
    npmDependencies in Compile += "aws-sdk" -> awsSdkVersion,
    useYarn := true,
  )
