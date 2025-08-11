ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.18"


// Determine OS for JavaFX dependencies
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

lazy val root = (project in file("."))
  .settings(
    name := "fooddistributionsystem",
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "8.0.192-R14",
      "org.scalatest" %% "scalatest" % "3.2.15" % Test
    ) ++ (
      if (System.getProperty("java.version").startsWith("1.8")) {
        Seq.empty
      } else {
        Seq("base", "controls", "fxml", "graphics", "media")
          .map(m => "org.openjfx" % s"javafx-$m" % "11.0.2" classifier osName)
      }
    ),
    fork := true,
    mainClass in Compile := Some("ui.FoodDistributionApp"),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-encoding", "utf8",
      "-feature"
    )
  )