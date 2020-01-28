
import sbt._

object MicroServiceBuild extends Build with MicroService {

  val appName = "awrs-notification"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val domainVersion = "5.6.0-play-26"
  private val playReactivemongoVersion = "7.22.0-play-26"
  private val hmrcTestVersion = "3.9.0-play-26"

  private val emailAddress = "3.4.0"
  private val mockitoVersion = "2.28.2"
  private val scalatestPlusPlayVersion = "3.1.2"
  private val jSoupVersion = "1.12.1"
  private val pegdownVersion = "1.6.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "simple-reactivemongo" % playReactivemongoVersion,
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-26" % "1.3.0",
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "uk.gov.hmrc" %% "emailaddress" % emailAddress
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test: Seq[ModuleID] = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-core" % mockitoVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalatestPlusPlayVersion % scope,
        "org.jsoup" % "jsoup" % jSoupVersion % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}
