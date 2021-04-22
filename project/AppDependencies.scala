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

import sbt._
import play.core.PlayVersion
import play.sbt.PlayImport._

private object AppDependencies {

  private val domainVersion = "5.11.0-play-27"
  private val playReactivemongoVersion = "8.0.0-play-27"
  private val hmrcTestVersion = "3.10.0-play-26"
  private val emailAddress = "3.5.0"
  private val mockitoVersion = "3.8.0"
  private val scalatestPlusPlayVersion = "4.0.3"
  private val jSoupVersion = "1.13.1"
  private val pegdownVersion = "1.6.0"
  private val bootstrapPlayVersion = "4.2.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "simple-reactivemongo"         % playReactivemongoVersion,
    "uk.gov.hmrc" %% "bootstrap-backend-play-27"    % bootstrapPlayVersion,
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
        "uk.gov.hmrc"            %%   "hmrctest"            % hmrcTestVersion          % scope,
        "org.pegdown"            %    "pegdown"             % pegdownVersion           % scope,
        "com.typesafe.play"      %%   "play-test"           % PlayVersion.current      % scope,
        "org.mockito"            %    "mockito-core"        % mockitoVersion           % scope,
        "org.scalatestplus.play" %%   "scalatestplus-play"  % scalatestPlusPlayVersion % scope,
        "org.jsoup"              %    "jsoup"               % jSoupVersion             % scope
      )
    }.test
  }

  object ITTest {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test: Seq[ModuleID] = Seq(
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalatestPlusPlayVersion % scope,
        "com.github.tomakehurst" % "wiremock-jre8" % "2.27.2" % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test() ++ ITTest()
}
