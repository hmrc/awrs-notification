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

import base.BaseSpec
import models.SendEmailRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.NO_CONTENT
import uk.gov.hmrc.connectors.ConnectorTest
import uk.gov.hmrc.emailaddress.EmailAddress
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

class EmailConnectorTest extends BaseSpec with MockitoSugar with ScalaFutures with GuiceOneAppPerSuite with ConnectorTest {

  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  val mockHttpClientV2: HttpClientV2 = mock[HttpClientV2]
  val mockServicesConfig: ServicesConfig = mock[ServicesConfig]

  val emailConnector = new EmailConnector(mockHttpClientV2, mockServicesConfig, "awrs-notification")

  val emailRequest: SendEmailRequest = SendEmailRequest(List(EmailAddress("test@email.com")), "fakeTemplateId", Map("key" -> "value"), force = true, None)

  implicit val mockHeaderCarrier: HeaderCarrier = HeaderCarrier()

  "sendEmail" should {
    "return 204 status when an email is sent successfully" in {

      when(requestBuilder.execute[HttpResponse](any, any)).thenReturn(Future.successful(HttpResponse.apply(NO_CONTENT, "")))
      val result = emailConnector.sendEmail(emailRequest)
      result must be(HttpResponse.apply(NO_CONTENT, ""))
    }
  }
}
