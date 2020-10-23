/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import play.sbt.routes.RoutesKeys

val appName: String = "awrs-notification"

lazy val appDependencies : Seq[ModuleID] = AppDependencies()
lazy val plugins : Seq[Plugins] = Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
lazy val playSettings : Seq[Setting[_]] = Seq.empty

RoutesKeys.routesImport := Seq.empty

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(

    ScoverageKeys.coverageExcludedPackages := "<empty>;app.*;config.*;Reverse.*;uk.gov.hmrc.*;prod.*;" +
      "testOnlyDoNotUseInAppConf.*;",

    ScoverageKeys.coverageMinimum := 80,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala,SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin) ++ plugins : _*)
  .settings(playSettings ++ scoverageSettings : _*)
  .settings( majorVersion := 3 )
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .configs(IntegrationTest)
  .settings(
    addTestReportOption(IntegrationTest, "int-test-reports"),
    inConfig(IntegrationTest)(Defaults.itSettings),
    Keys.fork in IntegrationTest :=  false,
    unmanagedSourceDirectories in IntegrationTest :=  (baseDirectory in IntegrationTest)(base => Seq(base / "it")).value,
    parallelExecution in IntegrationTest := false,
  )
  .settings(
    Keys.fork in Test := true,
    scalaVersion := "2.12.11",
    libraryDependencies ++= appDependencies,
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .settings(
    resolvers += Resolver.bintrayRepo("hmrc", "releases"),
    resolvers += Resolver.jcenterRepo
  )
  .disablePlugins(JUnitXmlReportPlugin)
