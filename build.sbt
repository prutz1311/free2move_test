val akkaVersion = "2.5.8"
val deps = Seq(
  // Akka
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % "10.0.11",

  // Logging
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
)

lazy val fetch = (project in file("fetch")).
  settings(
    name := "fetch",
    version := "0.1",
    scalaVersion := "2.12.4",
    libraryDependencies ++= deps
  )

lazy val interface = (project in file("interface")).
  settings(
    name := "interface",
    version := "0.1",
    scalaVersion := "2.12.4",
    libraryDependencies ++= deps
  )

lazy val fetch_separated = (project in file("fetch_separated")).
  settings(
    name := "fetch_separated",
    version := "0.1",
    scalaVersion := "2.12.4",
    libraryDependencies ++= deps
  )

lazy val fetch_compression = (project in file("fetch_compression")).
  settings(
    name := "fetch_compression",
    version := "0.1",
    scalaVersion := "2.12.4",
    libraryDependencies ++= deps
  )
