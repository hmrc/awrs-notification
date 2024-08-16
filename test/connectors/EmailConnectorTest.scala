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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.JsValue
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.emailaddress.EmailAddress
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

class EmailConnectorTest extends PlaySpec with MockitoSugar with ScalaFutures with GuiceOneAppPerSuite{

  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  val mockHttpClientV2: HttpClientV2 = mock[HttpClientV2]
  val mockServicesConfig: ServicesConfig = mock[ServicesConfig]
  val emailRequest: SendEmailRequest = SendEmailRequest(List(EmailAddress("test@email.com")), "fakeTemplateId", Map("key" -> "value"), force = true, None)
  val emailConnector =new  EmailConnector(mockHttpClientV2, mockServicesConfig, "awrs-notification")
  implicit val mockHeaderCarrier: HeaderCarrier = HeaderCarrier()

  trait ConnectorTest {
      val requestBuilder: RequestBuilder = mock[RequestBuilder]
      when(requestBuilder.withBody(any[JsValue])(any(), any(), any())).thenReturn(requestBuilder)
      def requestBuilderExecute[A]: Future[A] = requestBuilder.execute[A](any[HttpReads[A]], any[ExecutionContext])
  }

 "sendEmail" should {

    "return 204 status when an email is sent successfully" in new ConnectorTest {
      when(mockServicesConfig.baseUrl(any())).thenReturn("http://")
      when(mockHttpClientV2.post(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)
      when(requestBuilderExecute[HttpResponse]).thenReturn(Future.successful(HttpResponse.apply(NO_CONTENT, "")))

      val result: Future[HttpResponse] = emailConnector.sendEmail(emailRequest)
      await(result).status must be(NO_CONTENT)
    }
    }
  }
