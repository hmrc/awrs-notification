/*
 * Copyright 2021 HM Revenue & Customs
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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers._
import uk.gov.hmrc.emailaddress.EmailAddress
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import base.BaseSpec

import scala.concurrent.{ExecutionContext, Future}

class EmailConnectorTest extends BaseSpec with MockitoSugar with ScalaFutures with GuiceOneAppPerSuite {

  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  val mockDefaultHttpClient: DefaultHttpClient = mock[DefaultHttpClient]
  val mockServicesConfig: ServicesConfig = mock[ServicesConfig]

  val emailConnector = new EmailConnector(mockDefaultHttpClient, mockServicesConfig, "awrs-notification")

  val emailRequest = SendEmailRequest(List(EmailAddress("test@email.com")), "fakeTemplateId", Map("key" -> "value"), force = true, None)

  implicit val mockHeaderCarrier: HeaderCarrier = HeaderCarrier()

  "sendEmail" should {
    "return 204 status when an email is sent successfully" in {
      when(mockDefaultHttpClient.POST[Any, Any](any(), any(), any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse.apply(NO_CONTENT, "")))

      val result = await(emailConnector.sendEmail(emailRequest))

      result.status shouldBe NO_CONTENT
    }
  }
}
