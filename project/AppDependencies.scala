import sbt.*
import play.sbt.PlayImport.*

object AppDependencies {

  private val domainVersion = "11.0.0"
  private val hmrcMongoVersion = "2.7.0"
  private val bootstrapPlayVersion = "10.1.0"
  private val scalaCheckVersion = "1.18.1"
  private val scalaTestVersion = "3.2.19"
  private val scalaTestPlusVersion = "3.2.18.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc"       %% "domain-play-30"            % domainVersion,
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"   % bootstrapPlayVersion     % "test",
    "org.scalacheck"    %% "scalacheck"               % scalaCheckVersion        % "test",
    "org.scalatest"     %% "scalatest"                % scalaTestVersion         % "test",
    "org.scalatestplus" %% "scalacheck-1-17"          % scalaTestPlusVersion     % "test"
  )

  val itDependencies: Seq[ModuleID] = Seq()

  def apply(): Seq[ModuleID] = compile ++ test
}
