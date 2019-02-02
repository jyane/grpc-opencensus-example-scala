val defaultScalacOptions = Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-language:implicitConversions",
  "-Ywarn-dead-code",
  "-Ywarn-value-discard",
)

val commonSettings = Seq(
  scalaVersion := "2.12.5",
  scalacOptions ++= defaultScalacOptions,
)

val protoSettings = Seq(
  PB.targets in Compile := Seq(
    scalapb.gen(flatPackage = true) -> (sourceManaged in Compile).value
  ),
  PB.protoSources in Compile += file("proto"),
  libraryDependencies ++= Seq(
    "com.google.protobuf" % "protobuf-java" % scalapb.compiler.Version.protobufVersion % "protobuf",
    "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
    "io.grpc" % "grpc-services" % scalapb.compiler.Version.grpcJavaVersion,
    "io.opencensus" % "opencensus-exporter-stats-prometheus" % "0.18.0",
    "io.opencensus" % "opencensus-contrib-grpc-metrics" % "0.18.0",
    "io.opencensus" % "opencensus-impl" % "0.18.0",
    "io.prometheus" % "simpleclient_httpserver" % "0.4.0",
    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion,
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
  )
)

fork in run := true

lazy val server = (project in file("modules/server"))
  .settings(protoSettings)

lazy val client = (project in file("modules/client"))
  .settings(protoSettings)
