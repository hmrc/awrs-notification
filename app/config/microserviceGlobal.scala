/*
 * Copyright 2016 HM Revenue & Customs
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

package config

import com.typesafe.config.Config
import play.api.i18n.Messages
import play.api.mvc.RequestHeader
import play.api.mvc.Results._
import play.api.{Application, Configuration, Play}
import uk.gov.hmrc.play.audit.filters.AuditFilter
import uk.gov.hmrc.play.auth.controllers.AuthParamsControllerConfig
import uk.gov.hmrc.play.config.{AppName, ControllerConfig, RunMode}
import uk.gov.hmrc.play.http.logging.filters.LoggingFilter
import uk.gov.hmrc.play.microservice.bootstrap.DefaultMicroserviceGlobal
import uk.gov.hmrc.play.auth.microservice.filters.AuthorisationFilter
import net.ceedubs.ficus.Ficus._

import scala.concurrent.Future


object ControllerConfiguration extends ControllerConfig {
  lazy val controllerConfigs = Play.current.configuration.underlying.as[Config]("controllers")
}

object AuthParamsControllerConfiguration extends AuthParamsControllerConfig {
  lazy val controllerConfigs = ControllerConfiguration.controllerConfigs
}

object MicroserviceAuditFilter extends AuditFilter with AppName {
  override val auditConnector = AwrsNotificationAuditConnector
  override def controllerNeedsAuditing(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsAuditing
}

object MicroserviceLoggingFilter extends LoggingFilter {
  override def controllerNeedsLogging(controllerName: String) = ControllerConfiguration.paramsForController(controllerName).needsLogging
}

object MicroserviceAuthFilter extends AuthorisationFilter {
  override lazy val authParamsConfig = AuthParamsControllerConfiguration
  override lazy val authConnector = AwrsNotificationAuthConnector
  override def controllerNeedsAuth(controllerName: String): Boolean = ControllerConfiguration.paramsForController(controllerName).needsAuth
}

object MicroserviceGlobal extends DefaultMicroserviceGlobal with RunMode {
  import utils.JsonConstructor._

  override val auditConnector = AwrsNotificationAuditConnector

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig(s"$env.microservice.metrics")

  override val loggingFilter = MicroserviceLoggingFilter

  override val microserviceAuditFilter = MicroserviceAuditFilter

  override val authFilter = Some(MicroserviceAuthFilter)

  override def onBadRequest(request: RequestHeader, error: String) =
    Future.successful(BadRequest(constructErrorJson(error)))

  override def onHandlerNotFound(request: RequestHeader) =
    Future.successful(NotFound(constructErrorJson(Messages("uri.invalid"))))

}
