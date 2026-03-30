name := "sbt-github-annotator"

sbtPlugin := true

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.2.20" % Test,
  "org.scalatest" %% "scalatest" % "3.2.20" % Test
)

val scala212 = "2.12.20"
val scala3   = "3.8.2"
ThisBuild / crossScalaVersions := Seq(scala212, scala3)
ThisBuild / scalaVersion       := scala3

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-github-annotator",
    addSbtPlugin("com.github.sbt" % "sbt2-compat" % "0.1.0"),
    scalacOptions ++= {
      scalaBinaryVersion.value match {
        case "3" => Nil
        case _   =>
          Seq(
            "-Xsource:3",
            "-Xfuture"
          )
      }
    },
    (pluginCrossBuild / sbtVersion) := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.5.8"
        case _      => "2.0.0-RC10"
      }
    }
  )

inThisBuild(
  List(
    tlBaseVersion          := "2.0",
    organization           := "io.github.mouwrice",
    organizationName       := "Maurice Van Wassenhove",
    startYear              := Some(2025),
    licenses               := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
    developers             := List(tlGitHubDev("mouwrice", "Maurice Van Wassenhove")),
    description            := "Sbt plugin that annotates GitHub pull requests",
    tlCiDependencyGraphJob := true,
    tlCiForkCondition      := "true" // Do not check for forks
  )
)

enablePlugins(AutomateHeaderPlugin)
headerLicenseStyle := HeaderLicenseStyle.SpdxSyntax

enablePlugins(TypelevelUnidocPlugin)

enablePlugins(ScriptedPlugin)
// set up scripted; sbt plugin for testing sbt plugins
scriptedLaunchOpts ++=
  Seq("-Xmx1024M", "-Dplugin.version=" + version.value)

wartremoverErrors ++= Warts.allBut(
  Wart.Throw,
  Wart.Equals,
  Wart.Option2Iterable,
  Wart.DefaultArguments,
  Wart.Overloading,
  Wart.ReverseFind,
  Wart.NonUnitStatements
)
