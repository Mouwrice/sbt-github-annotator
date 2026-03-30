/*
 * Copyright 2025 Maurice Van Wassenhove
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.mouwrice

import sbt.*
import Keys.*

sealed trait AnnotationOrigin
object AnnotationOrigin {
  case object Compilation extends AnnotationOrigin
  case object Testing     extends AnnotationOrigin
}
sealed trait AnnotationSeverity
object AnnotationSeverity {
  case object Warning extends AnnotationSeverity
  case object Error   extends AnnotationSeverity
}
trait AnnotationFilter {

  /** Filter for annotations */
  def filter(
      origin: AnnotationOrigin,
      severity: AnnotationSeverity,
      title: String,
      fileName: Option[String],
      lineNumber: Option[Int]
  ): Boolean
}

trait Annotator {
  def createAnnotation(
      origin: AnnotationOrigin,
      severity: AnnotationSeverity,
      title: String,
      fileName: Option[String] = None,
      lineNumber: Option[Int] = None
  ): Unit
  def createAnnotation(
      origin: AnnotationOrigin,
      severity: AnnotationSeverity,
      title: String,
      fileName: String,
      lineNumber: Int
  ): Unit =
    createAnnotation(origin, severity, title, Some(fileName), Some(lineNumber))
  def error(origin: AnnotationOrigin, title: String, fileName: String, lineNumber: Int): Unit =
    createAnnotation(origin, AnnotationSeverity.Error, title, fileName, lineNumber)
  def error(origin: AnnotationOrigin, title: String): Unit =
    createAnnotation(origin, AnnotationSeverity.Error, title)
  def warn(origin: AnnotationOrigin, title: String, fileName: String, lineNumber: Int): Unit =
    createAnnotation(origin, AnnotationSeverity.Warning, title, fileName, lineNumber)
  def warn(origin: AnnotationOrigin, title: String): Unit =
    createAnnotation(origin, AnnotationSeverity.Warning, title)
}

object GithubAnnotatorPlugin extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger  = allRequirements

  object autoImport {
    lazy val annotateTestFailures: SettingKey[Boolean] =
      settingKey[Boolean]("Whether test failures should be annotated")
    lazy val annotateCompileWarnings: SettingKey[Boolean] =
      settingKey[Boolean]("Whether compilation warnings should be annotated")
    lazy val annotateCompileErrors: SettingKey[Boolean] =
      settingKey[Boolean]("Whether compilation errors should be annotated")
    lazy val annotationFilter: SettingKey[AnnotationFilter] =
      settingKey[AnnotationFilter](
        "Can be overridden to do fine-grained filtering for annotations. Will override whatever the boolean flag keys have set"
      )
    lazy val annotationFilePathPrefix: SettingKey[String] = settingKey[String](
      "Path prefix for the file paths in the annotations. Used for when your project is not at the root of your github repository."
    )

    private[GithubAnnotatorPlugin] val annotator = settingKey[Annotator]("")
  }
  import autoImport.*

  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Nothing"))
  override def projectSettings: Seq[Def.Setting[?]] =
    if (sys.env.contains("GITHUB_ENV"))
      Seq(
        reporterFor(Compile),
        reporterFor(Test),
        testListeners +=
          new GithubAnnotationTestsListener(
            annotator.value,
            (ThisBuild / baseDirectory).value,
            (Test / sourceDirectories).value
          )
      )
    else
      Nil

  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Nothing"))
  override def globalSettings: Seq[Def.Setting[?]] = Seq(
    annotateTestFailures     := true,
    annotateCompileWarnings  := true,
    annotateCompileErrors    := true,
    annotationFilePathPrefix := ".",
    annotationFilter         := {
      val testFailures    = annotateTestFailures.value
      val compileWarnings = annotateCompileWarnings.value
      val compileErrors   = annotateCompileErrors.value
      new AnnotationFilter {
        override def filter(
            origin: AnnotationOrigin,
            severity: AnnotationSeverity,
            title: String,
            fileName: Option[String],
            lineNumber: Option[Int]
        ): Boolean = {
          origin match {
            case AnnotationOrigin.Testing     => testFailures
            case AnnotationOrigin.Compilation =>
              severity match {
                case AnnotationSeverity.Warning => compileWarnings
                case AnnotationSeverity.Error   => compileErrors
              }
          }
        }
      }
    },
    annotator := createAnnotator(annotationFilter.value)
  )

  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Nothing"))
  private def reporterFor(config: Configuration): Setting[?] =
    config / compile / InternalAccess.compilerReporter :=
      new GithubActionCompileReporter(
        annotator.value,
        (config / compile / InternalAccess.compilerReporter).value,
        (ThisBuild / baseDirectory).value,
        annotationFilePathPrefix.value
      )

  private def createAnnotator(filter: AnnotationFilter): Annotator = (
      origin: AnnotationOrigin,
      severity: AnnotationSeverity,
      title: String,
      fileName: Option[String],
      lineNumber: Option[Int]
  ) =>
    if (filter.filter(origin, severity, title, fileName, lineNumber)) {
      val severityTag = severity match {
        case AnnotationSeverity.Warning => "warning"
        case AnnotationSeverity.Error   => "error"
      }

      def e(key: String, value: Any): String = s"$key=$value"

      val entries: String = (fileName.map(e("file", _)) ++ lineNumber.map(e("line", _))).mkString(",")

      println(s"::$severityTag $entries::$title")
    }
}
