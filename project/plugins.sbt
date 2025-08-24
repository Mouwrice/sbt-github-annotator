libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
addSbtPlugin("com.github.sbt"           % "sbt-ci-release"  % "1.11.2")
//addSbtPlugin("com.codecommit"           % "sbt-github-actions" % "0.13.0")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.5")
