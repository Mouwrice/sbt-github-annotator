/*
 * Copyright 2025 Maurice Van Wassenhove
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.virtualvoid.hackersdigest

object TestStackTraceInfoExtractor {
  def mostRelevantTraceElement(stackTrace: Array[StackTraceElement]): Option[StackTraceElement] =
    stackTrace
      .takeWhile(!_.getClassName.endsWith("OutcomeOf"))
      .toSeq
      .reverse
      .find(e => !InfraPackages.exists(e.getClassName.startsWith(_)))

  private val InfraPackages = Set("org.scalatest", "scala")
}
