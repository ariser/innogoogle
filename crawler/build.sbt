name := "wikicrawler"

version := "1.0"

scalaVersion := "2.11.8"

lazy val doobieVersion = "0.3.0"

libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "1.1.0"
libraryDependencies += "org.tpolecat" %% "doobie-core" % doobieVersion
libraryDependencies += "org.tpolecat" %% "doobie-contrib-postgresql" % doobieVersion
libraryDependencies += "org.tpolecat" %% "doobie-contrib-specs2" % doobieVersion