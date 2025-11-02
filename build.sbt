import sbt.Keys.homepage

name := """sbt-github-annotator"""

sbtPlugin := true

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.2.19" % "test",
  "org.scalatest" %% "scalatest" % "3.2.19" % "test"
)

inThisBuild(
  List(
    organization := "io.github.mouwrice",
    homepage     := Some(url("https://github.com/Mouwrice/sbt-github-annotator")),
    licenses     := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "mouwrice",
        "Maurice Van Wassenhove",
        "mauricevanwassenhove@fastmail.com",
        url("https://github.com/mouwrice")
      )
    )
  )
)

ThisBuild / versionScheme := Some("semver-spec")

console / initialCommands := """import net.virtualvoid.hackersdigest._"""

enablePlugins(ScriptedPlugin)
// set up scripted; sbt plugin for testing sbt plugins
scriptedLaunchOpts ++=
  Seq("-Xmx1024M", "-Dplugin.version=" + version.value)

//ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
//ThisBuild / githubWorkflowPublishTargetBranches :=
//  Seq(RefPredicate.StartsWith(Ref.Tag("v")))
//
//ThisBuild / githubWorkflowPublish := Seq(
//  WorkflowStep.Sbt(
//    List("ci-release"),
//    env = Map(
//      "PGP_PASSPHRASE"    -> "${{ secrets.PGP_PASSPHRASE }}",
//      "PGP_SECRET"        -> "${{ secrets.PGP_SECRET }}",
//      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
//      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
//    )
//  )
//)

wartremoverErrors ++= Warts.allBut(
  Wart.Throw,
  Wart.Equals,
  Wart.Option2Iterable,
  Wart.DefaultArguments,
  Wart.Overloading,
  Wart.ReverseFind,
  Wart.NonUnitStatements
)
