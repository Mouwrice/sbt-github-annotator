libraryDependencies += "org.scala-sbt" %% "scripted-plugin"    % sbtVersion.value
addSbtPlugin("com.geirsson"             % "sbt-ci-release"     % "1.5.7")
addSbtPlugin("com.codecommit"           % "sbt-github-actions" % "0.14.2")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
