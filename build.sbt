name := """sisdn-admission-service"""

version := "1.0"

scalaVersion := "2.11.7"

val akkaV       = "2.4.0"
val akkaStreamV = "1.0"
val scalaTestV  = "2.2.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"                           % akkaV,
  "com.typesafe.akka" %% "akka-stream-experimental"             % akkaStreamV,
  "com.typesafe.akka" %% "akka-http-core-experimental"          % akkaStreamV,
  "com.typesafe.akka" %% "akka-http-experimental"               % akkaStreamV,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental"    % akkaStreamV,
  "com.typesafe.akka" %% "akka-http-testkit-experimental"       % akkaStreamV,
  "org.scalatest"     %% "scalatest"                            % scalaTestV % "test")
