package sbt.hackersdigest

import sbt.TaskKey
import xsbti.Reporter

object InternalAccess {
  val compilerReporter: TaskKey[Reporter] = _root_.sbt.Keys.compilerReporter
}
