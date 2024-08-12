import sbt.*
import play.sbt.PlayImport.*

object AppDependencies {

  private val domainVersion = "10.0.0"
  private val hmrcMongoVersion = "1.7.0"
  private val emailAddress = "4.0.0"
  private val mockitoVersion = "5.11.0"
  private val scalatestPlusPlayVersion = "7.0.1"
  private val jSoupVersion = "1.18.1"
  private val bootstrapPlayVersion = "9.2.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc"       %% "domain-play-30"            % domainVersion,
    "uk.gov.hmrc"       %% "emailaddress-play-30"      % emailAddress
  )

  val test: Seq[ModuleID] = Seq(
    "org.mockito"            %  "mockito-core"           % mockitoVersion           % "test",
    "org.scalatestplus.play" %% "scalatestplus-play"     % scalatestPlusPlayVersion % "test",
    "org.scalatestplus"      %% "scalatestplus-mockito"  % "1.0.0-M2"               % "test",
    "org.jsoup"              %  "jsoup"                  % jSoupVersion             % "test",
    "uk.gov.hmrc"            %% "bootstrap-test-play-30" % bootstrapPlayVersion     % "test"
  )

  val itDependencies: Seq[ModuleID] = Seq(
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.17.2" % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
