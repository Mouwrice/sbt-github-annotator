/*
 * Copyright 2025 Maurice Van Wassenhove
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package sbt

import xsbti.Reporter

object InternalAccess {
  val compilerReporter: TaskKey[Reporter] = _root_.sbt.Keys.compilerReporter
}
