name := "traffic_light_playws_client"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= Seq (
  "com.typesafe.play" %% "play-ahc-ws-standalone" % "2.1.2",
  "com.typesafe.play" %% "play-ws-standalone-json" % "2.1.2"
)