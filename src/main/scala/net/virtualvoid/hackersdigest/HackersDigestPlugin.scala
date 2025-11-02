package net.virtualvoid.hackersdigest

import sbt.*
import Keys.*
import sbt.hackersdigest.InternalAccess

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

object HackersDigestPlugin extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger  = allRequirements

  object autoImport {
    val hackersDigestAnnotateTestFailures: SettingKey[Boolean] =
      settingKey[Boolean]("Whether test failures should be annotated")
    val hackersDigestAnnotateCompileWarnings: SettingKey[Boolean] =
      settingKey[Boolean]("Whether compilation warnings should be annotated")
    val hackersDigestAnnotateCompileErrors: SettingKey[Boolean] =
      settingKey[Boolean]("Whether compilation errors should be annotated")
    val hackersDigestAnnotationFilter: SettingKey[AnnotationFilter] =
      settingKey[AnnotationFilter](
        "Can be overridden to do fine-grained filtering for annotations. Will override whatever the boolean flag keys have set"
      )
    val hackersDigestFilePathPrefix: SettingKey[String] = settingKey[String](
      "Path prefix for the file paths in the annotations. Used for when your project is not at the root of your github repository."
    )

    private[HackersDigestPlugin] val hackersDigestAnnotator = settingKey[Annotator]("")
  }
  import autoImport._

  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Nothing"))
  override def projectSettings: Seq[Def.Setting[?]] =
    if (sys.env.contains("GITHUB_ENV"))
      Seq(
        reporterFor(Compile),
        reporterFor(Test),
        testListeners +=
          new GithubAnnotationTestsListener(
            hackersDigestAnnotator.value,
            (ThisBuild / baseDirectory).value,
            (Test / sourceDirectories).value
          )
      )
    else
      Nil

  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Nothing"))
  override def globalSettings: Seq[Def.Setting[?]] = Seq(
    hackersDigestAnnotateTestFailures    := true,
    hackersDigestAnnotateCompileWarnings := true,
    hackersDigestAnnotateCompileErrors   := true,
    hackersDigestFilePathPrefix          := ".",
    hackersDigestAnnotationFilter := {
      val annotateTestFailures = hackersDigestAnnotateTestFailures.value
      val compileWarnings      = hackersDigestAnnotateCompileWarnings.value
      val compileErrors        = hackersDigestAnnotateCompileErrors.value
      new AnnotationFilter {
        override def filter(
            origin: AnnotationOrigin,
            severity: AnnotationSeverity,
            title: String,
            fileName: Option[String],
            lineNumber: Option[Int]
        ): Boolean = {
          origin match {
            case AnnotationOrigin.Testing => annotateTestFailures
            case AnnotationOrigin.Compilation =>
              severity match {
                case AnnotationSeverity.Warning => compileWarnings
                case AnnotationSeverity.Error   => compileErrors
              }
          }
        }
      }
    },
    hackersDigestAnnotator := annotator(hackersDigestAnnotationFilter.value)
  )

  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Nothing"))
  private def reporterFor(config: Configuration): Setting[?] =
    config / compile / InternalAccess.compilerReporter :=
      new GithubActionCompileReporter(
        hackersDigestAnnotator.value,
        (config / compile / InternalAccess.compilerReporter).value,
        (ThisBuild / baseDirectory).value,
        hackersDigestFilePathPrefix.value
      )

  private def annotator(filter: AnnotationFilter): Annotator = new Annotator {
    override def createAnnotation(
        origin: AnnotationOrigin,
        severity: AnnotationSeverity,
        title: String,
        fileName: Option[String],
        lineNumber: Option[Int]
    ): Unit =
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
}
