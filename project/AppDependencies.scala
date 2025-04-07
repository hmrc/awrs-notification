import sbt.*
import play.sbt.PlayImport.*

object AppDependencies {

  private val domainVersion = "11.0.0"
  private val hmrcMongoVersion = "2.6.0"
  private val emailAddress = "4.1.0"
  private val bootstrapPlayVersion = "9.11.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc"       %% "domain-play-30"            % domainVersion,
    "uk.gov.hmrc"       %% "emailaddress-play-30"      % emailAddress
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"      %% "bootstrap-test-play-30" % bootstrapPlayVersion     % "test"
  )

  val itDependencies: Seq[ModuleID] = Seq()

  def apply(): Seq[ModuleID] = compile ++ test
}
