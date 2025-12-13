name := "sbt-github-annotator"

sbtPlugin := true

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.2.19" % Test,
  "org.scalatest" %% "scalatest" % "3.2.19" % Test
)

inThisBuild(
  List(
    tlBaseVersion    := "2.0",
    organization     := "io.github.mouwrice",
    organizationName := "Maurice Van Wassenhove",
    startYear        := Some(2025),
    licenses         := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
    tlGitHubRepo     := Some("sbt-github-annotator"),
    developers := List(
      tlGitHubDev("mouwrice", "Maurice Van Wassenhove")
    ),
    tlCiDependencyGraphJob := true
  )
)

enablePlugins(AutomateHeaderPlugin)
headerLicenseStyle := HeaderLicenseStyle.SpdxSyntax

enablePlugins(TypelevelUnidocPlugin)

console / initialCommands := """import net.virtualvoid.hackersdigest._"""

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
