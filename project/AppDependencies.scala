import sbt._
import play.core.PlayVersion
import play.sbt.PlayImport._

private object AppDependencies {

  private val domainVersion = "6.2.0-play-28"
  private val playReactivemongoVersion = "8.0.0-play-28"
  private val emailAddress = "3.5.0"
  private val mockitoVersion = "4.0.0"
  private val scalatestPlusPlayVersion = "5.1.0"
  private val jSoupVersion = "1.14.3"
  private val pegdownVersion = "1.6.0"
  private val bootstrapPlayVersion = "5.16.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "simple-reactivemongo"         % playReactivemongoVersion,
    "uk.gov.hmrc" %% "bootstrap-backend-play-28"    % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "domain"                       % domainVersion,
    "uk.gov.hmrc" %% "emailaddress"                 % emailAddress
  )

  trait TestDependencies {
    lazy val scope: String = "it,test"
    lazy val test : Seq[ModuleID] = Nil
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test: Seq[ModuleID] = Seq(
        "org.pegdown"            %    "pegdown"                    % pegdownVersion           % scope,
        "com.typesafe.play"      %%   "play-test"                  % PlayVersion.current      % scope,
        "org.mockito"            %    "mockito-core"               % mockitoVersion           % scope,
        "org.scalatestplus.play" %%   "scalatestplus-play"         % scalatestPlusPlayVersion % scope,
        "org.scalatestplus"      %%   "scalatestplus-mockito"      % "1.0.0-M2"               % scope,
        "com.typesafe.play"      %%   "play-test"                  % PlayVersion.current      % scope,
        "org.jsoup"              %    "jsoup"                      % jSoupVersion             % scope,
        "uk.gov.hmrc"            %%   "bootstrap-test-play-28"     % bootstrapPlayVersion     % scope
      )
    }.test
  }

  object ITTest {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test: Seq[ModuleID] = Seq(
        "org.pegdown"                  % "pegdown"                  % pegdownVersion % scope,
        "com.typesafe.play"            %% "play-test"               % PlayVersion.current % scope,
        "org.scalatestplus.play"       %% "scalatestplus-play"      % scalatestPlusPlayVersion % scope,
        "com.github.tomakehurst"       %  "wiremock-jre8"           % "2.31.0" % scope,
        "com.fasterxml.jackson.module" %% "jackson-module-scala"    % "2.13.0" % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test() ++ ITTest()
}
