import sbt._
import play.core.PlayVersion
import play.sbt.PlayImport._

object AppDependencies {

  private val domainVersion = "8.3.0-play-28"
  private val hmrcMongoVersion = "0.70.0"
  private val emailAddress = "3.8.0"
  private val mockitoVersion = "5.4.0"
  private val scalatestPlusPlayVersion = "5.1.0"
  private val jSoupVersion = "1.16.1"
  private val pegdownVersion = "1.6.0"
  private val bootstrapPlayVersion = "7.19.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"        % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc"       %% "domain"                    % domainVersion,
    "uk.gov.hmrc"       %% "emailaddress"              % emailAddress
  )

  trait TestDependencies {
    lazy val scope: String = "it,test"
    lazy val test : Seq[ModuleID] = Nil
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test: Seq[ModuleID] = Seq(
        "org.pegdown"            %  "pegdown"                % pegdownVersion           % scope,
        "com.typesafe.play"      %% "play-test"              % PlayVersion.current      % scope,
        "org.mockito"            %  "mockito-core"           % mockitoVersion           % scope,
        "org.scalatestplus.play" %% "scalatestplus-play"     % scalatestPlusPlayVersion % scope,
        "org.scalatestplus"      %% "scalatestplus-mockito"  % "1.0.0-M2"               % scope,
        "org.jsoup"              %  "jsoup"                  % jSoupVersion             % scope,
        "uk.gov.hmrc"            %% "bootstrap-test-play-28" % bootstrapPlayVersion     % scope
      )
    }.test
  }

  object ITTest {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test: Seq[ModuleID] = Seq(
        "org.pegdown"                  %  "pegdown"              % pegdownVersion % scope,
        "com.github.tomakehurst"       %  "wiremock-jre8"        % "2.35.0"       % scope,
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.2"       % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test() ++ ITTest()
}
