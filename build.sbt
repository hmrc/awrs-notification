import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}

val appName: String = "awrs-notification"

ThisBuild / majorVersion := 3
ThisBuild / scalaVersion := "2.13.16"

lazy val appDependencies : Seq[ModuleID] = AppDependencies()
lazy val playSettings : Seq[Setting[_]] = Seq.empty

RoutesKeys.routesImport := Seq.empty

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(

    ScoverageKeys.coverageExcludedPackages := "<empty>;app.*;config.*;Reverse.*;uk.gov.hmrc.*;prod.*;" +
      "testOnlyDoNotUseInAppConf.*;",

    ScoverageKeys.coverageMinimumStmtTotal := 80,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(playSettings ++ scoverageSettings : _*)
  .settings(scalaSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    scalacOptions ++= Seq("-feature", "-Wconf:src=routes/.*:s"),
    libraryDependencies ++= appDependencies,
    retrieveManaged := true
  )
  .settings(
    Test / Keys.fork := true,
  )
  .settings(
    resolvers ++= Seq()
  )
  .disablePlugins(JUnitXmlReportPlugin)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.itDependencies)
