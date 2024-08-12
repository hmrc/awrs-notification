/*
 * Copyright 2023 HM Revenue & Customs
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

package connectors

import models.SendEmailRequest
import play.api.Logging
import play.api.libs.json.{Json, OWrites}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class EmailConnector @Inject()(http: HttpClientV2,
                                 val config: ServicesConfig,
                                 @Named("appName") val appName: String)(implicit ec: ExecutionContext) extends Logging {

  private lazy val serviceURL: String = config.baseUrl(serviceName = "email")
  implicit val sendEmailRequestWrites: OWrites[SendEmailRequest] = Json.writes[SendEmailRequest]
  private val sendEmailURI = "/hmrc/email"

  def sendEmail(emailData: SendEmailRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val postUrl = s"""$serviceURL$sendEmailURI"""

    println(serviceURL)


    http.post(url"$postUrl").withBody(emailData.toString).execute[HttpResponse]. map {
      response =>
        logger.warn("[API12] Send Email request sent to EMAIL microservice" + response.body)
        response
    }
  }
}
