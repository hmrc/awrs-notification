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

package controllers

import audit.TestAudit
import models.EmailResponse
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.mvc.{ControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import services.EmailService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.Audit
import uk.gov.hmrc.play.test._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class EmailControllerTest extends UnitSpec with MockitoSugar with ScalaFutures with GuiceOneAppPerSuite {

  val mockEmailService: EmailService = mock[EmailService]
  val mockAuditConnector: AuditConnector = mock[AuditConnector]
  val cc: ControllerComponents = app.injector.instanceOf[ControllerComponents]

  val emailController = new EmailController(mockAuditConnector, mockEmailService, cc, "awrs-notification")

  trait EmailControllerFixture {
    val emailController: EmailController = new EmailController(mockAuditConnector, mockEmailService, cc, "awrs-notification") {
      override val audit: Audit = new TestAudit(mockAuditConnector)
    }
  }

  implicit val mockHeaderCarrier: HeaderCarrier = HeaderCarrier()

  "EmailController for notification" should {

    "return 204 status when the email is sent successfully" in {
      when(mockEmailService.sendNotificationEmail(any(), any(), any())(any())).thenReturn(Future.successful(EmailResponse(OK, None)))

      val result = emailController.sendNotificationEmail("").apply(FakeRequest().withJsonBody(Json.obj()))
      status(result) shouldBe NO_CONTENT
    }

    "return 400 status when the input json fails validation" in {
      when(mockEmailService.sendNotificationEmail(any(), any(), any())(any())).thenReturn(
        Future.successful(EmailResponse(BAD_REQUEST, Some("Bad Thing Happened")))
      )

      val result = Await.result(emailController.sendNotificationEmail("").apply(FakeRequest().withJsonBody(Json.obj())), 2.second)

      status(result) shouldBe BAD_REQUEST
      val document = Jsoup.parse(contentAsString(result))

      document.toString should include("{\"reason\":\"Bad Thing Happened\"}")
    }

    "return 400 status when the input json fails validation (empty response body)" in {
      when(mockEmailService.sendNotificationEmail(any(), any(), any())(any())).thenReturn(Future.successful(EmailResponse(BAD_REQUEST, Some(""))))

      val result = Await.result(emailController.sendNotificationEmail("").apply(FakeRequest().withJsonBody(Json.obj())), 2.second)

      status(result) shouldBe BAD_REQUEST
      val document = Jsoup.parse(contentAsString(result))

      document.toString should include("{\"reason\":\"Unknown reason. Dependent system did not provide failure reason\"}")
    }

    "return 400 status when the input json fails validation (no errors)" in {
      when(mockEmailService.sendNotificationEmail(any(), any(), any())(any())).thenReturn(Future.successful(EmailResponse(BAD_REQUEST, None)))

      val result = Await.result(emailController.sendNotificationEmail("").apply(FakeRequest().withJsonBody(Json.obj())), 2.second)

      status(result) shouldBe BAD_REQUEST
      val document = Jsoup.parse(contentAsString(result))

      document.toString should include("{\"reason\":\"Unknown reason. Dependent system did not provide failure reason\"}")
    }

    "return 404 status when the email template is not found" in {
      when(mockEmailService.sendNotificationEmail(any(), any(), any())(any())).thenReturn(
        Future.successful(EmailResponse(NOT_FOUND, Some("Invalid template")))
      )

      val result = Await.result(emailController.sendNotificationEmail("").apply(FakeRequest().withJsonBody(Json.obj())), 2.second)

      status(result) shouldBe NOT_FOUND
      val document = Jsoup.parse(contentAsString(result))

      document.toString should include("{\"reason\":\"Invalid template\"}")
    }

    "return 500 status when the email connector fails for external reasons (template not found or validation error occurred in external Email service)" in {
      when(mockEmailService.sendNotificationEmail(any(), any(), any())(any())).thenReturn(
        Future.successful(EmailResponse(INTERNAL_SERVER_ERROR, Some("Invalid template")))
      )

      val result = Await.result(emailController.sendNotificationEmail("").apply(FakeRequest().withJsonBody(Json.obj())), 2.second)

      status(result) shouldBe INTERNAL_SERVER_ERROR
      val document = Jsoup.parse(contentAsString(result))

      document.toString should include("{\"reason\":\"Invalid template\"}")
    }

    "return 500 status when the request is of wrong type" in {
      val result = Await.result(emailController.sendNotificationEmail("").apply(FakeRequest().withTextBody("TEXT")), 2.second)

      status(result) shouldBe INTERNAL_SERVER_ERROR
      val document = Jsoup.parse(contentAsString(result))

      document.toString should include("{\"reason\":\"Invalid request content type\"}")
    }

    "return 503 status when email connector fails for external reasons" in {
      when(mockEmailService.sendNotificationEmail(any(), any(), any())(any())).thenReturn(
        Future.successful(EmailResponse(SERVICE_UNAVAILABLE, Some("Something Bad Happened")))
      )

      val result = Await.result(emailController.sendNotificationEmail("")
        .apply(FakeRequest().withJsonBody(Json.obj())), 2.second)

      status(result) shouldBe SERVICE_UNAVAILABLE
      val document = Jsoup.parse(contentAsString(result))

      document.toString should include("{\"reason\":\"Something Bad Happened\"}")
    }

    "return 503 status when email connector fails for external reasons (empty response)" in {
      when(mockEmailService.sendNotificationEmail(any(), any(), any())(any())).thenReturn(
        Future.successful(EmailResponse(SERVICE_UNAVAILABLE, Some("")))
      )

      val result = Await.result(emailController.sendNotificationEmail("").apply(FakeRequest().withJsonBody(Json.obj())), 2.second)

      status(result) shouldBe SERVICE_UNAVAILABLE
      val document = Jsoup.parse(contentAsString(result))

      document.toString should include("{\"reason\":\"Unknown reason. Dependent system did not provide failure reason\"}")
    }

    "receive event - return 200 status when a valid json is received with eventType as delivered " in new EmailControllerFixture {
      val callBackResponseJson = """{"events": [ {"event": "delivered", "detected": "2015-07-02T08:26:39.035Z" }]}"""
      val result: Future[Result] =
        emailController.receiveEvent("firstName", "XFS00000123456").apply(FakeRequest().withJsonBody(Json.parse(callBackResponseJson)))

      status(result) shouldBe OK
    }

    "receive event - return 200 status when a valid json is received with eventType as permanentBounce" in new EmailControllerFixture {
      val callBackResponseJson = """{"events": [ {"event": "permanentBounce", "detected": "2015-07-02T08:26:39.035Z" }]}"""
      val result: Future[Result] =
        emailController.receiveEvent("firstName", "XFS00000123456").apply(FakeRequest().withJsonBody(Json.parse(callBackResponseJson)))

      status(result) shouldBe OK
    }

    "receive event - return 200 status when a valid json is received with eventType as Sent" in new EmailControllerFixture {
      val callBackResponseJson = """{"events": [ {"event": "Sent", "detected": "2015-07-02T08:26:39.035Z" }]}"""
      val result: Future[Result] =
        emailController.receiveEvent("firstName", "XFS00000123456").apply(FakeRequest().withJsonBody(Json.parse(callBackResponseJson)))

      status(result) shouldBe OK
    }

    "receive event - return 500 status when a invalid json is received" in new EmailControllerFixture {
      val callBackResponseJson = """{"eventInvalid": [ {"event": "Sent", "detected": "2015-07-02T08:26:39.035Z" }]}"""
      val result: Future[Result] =
        emailController.receiveEvent("firstName", "XFS00000123456").apply(FakeRequest().withJsonBody(Json.parse(callBackResponseJson)))

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "receive event - return 500 status when invalid content Type is received" in new EmailControllerFixture {
      val result: Future[Result] =
        emailController.receiveEvent("firstName", "XFS00000123456").apply(FakeRequest().withTextBody("You naughty!"))

      whenReady(result) {
        result =>
          status(result) shouldBe INTERNAL_SERVER_ERROR
          val document = Jsoup.parse(contentAsString(result))

          document.toString should include("{\"reason\":\"Invalid request content type\"}")
      }
    }
  }

  "EmailController for withdrawn" should {
    "return 204 status when the email is sent succesfully" in {
      when(mockEmailService.sendWithdrawnEmail(any(), any())(any())).thenReturn(Future.successful(EmailResponse(OK, None)))

      val result = emailController.sendWithdrawnEmail.apply(FakeRequest().withJsonBody(Json.obj()))
      status(result) shouldBe NO_CONTENT
    }

    "return 400 status when the input json fails validation" in {
      when(mockEmailService.sendWithdrawnEmail(any(), any())(any())).thenReturn(Future.successful(EmailResponse(BAD_REQUEST, Some("Error"))))

      val result = Await.result(emailController.sendWithdrawnEmail().apply(FakeRequest().withJsonBody(Json.obj())), 2.second)

      status(result) shouldBe BAD_REQUEST
      val document = Jsoup.parse(contentAsString(result))

      document.toString should include("{\"reason\":\"Error\"}")
    }

    "receive event - return 200 status when a valid json is received with eventType as delivered " in new EmailControllerFixture {
      val callBackResponseJson = """{"events": [ {"event": "delivered", "detected": "2015-07-02T08:26:39.035Z" }]}"""
      val result: Future[Result] =
        emailController.receiveEmailEvent("API8", "XFS00000123456",
          "10 September 2016").apply(FakeRequest().withJsonBody(Json.parse(callBackResponseJson)))
      status(result) shouldBe OK

    }

    "receive event - return 500 status when a invalid json is received" in new EmailControllerFixture {
      val callBackResponseJson = """{"eventInvalid": [ {"event": "Sent", "detected": "2015-07-02T08:26:39.035Z" }]}"""
      val result: Future[Result] =
        emailController.receiveEmailEvent("API8", "XFS00000123456",
          "10 September 2016").apply(FakeRequest().withJsonBody(Json.parse(callBackResponseJson)))

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "receive event - return 500 status when invalid content Type is received" in new EmailControllerFixture {
      val result: Future[Result] =
        emailController.receiveEmailEvent("API8", "XFS00000123456",
          "10 September 2016").apply(FakeRequest().withTextBody("Error"))

      whenReady(result) {
        result =>
          status(result) shouldBe INTERNAL_SERVER_ERROR
          val document = Jsoup.parse(contentAsString(result))

          document.toString should include("{\"reason\":\"Invalid request content type\"}")
      }
    }
  }

  "EmailController for cancellation" should {
    "return 204 status when the email is sent succesfully" in {
      when(mockEmailService.sendCancellationEmail(any(), any())(any())).thenReturn(Future.successful(EmailResponse(OK, None)))

      val result = emailController.sendCancellationEmail.apply(FakeRequest().withJsonBody(Json.obj()))
      status(result) shouldBe NO_CONTENT
    }

    "return 400 status when the input json fails validation" in {
      when(mockEmailService.sendCancellationEmail(any(), any())(any())).thenReturn(Future.successful(EmailResponse(BAD_REQUEST, Some("Error"))))

      val result = Await.result(emailController.sendCancellationEmail().apply(FakeRequest().withJsonBody(Json.obj())), 2.second)

      status(result) shouldBe BAD_REQUEST
      val document = Jsoup.parse(contentAsString(result))

      document.toString should include("{\"reason\":\"Error\"}")
    }

    "receive event - return 200 status when a valid json is received with eventType as delivered " in new EmailControllerFixture {
      val callBackResponseJson = """{"events": [ {"event": "delivered", "detected": "2015-07-02T08:26:39.035Z" }]}"""
      val result: Future[Result] =
        emailController.receiveEmailEvent("API8", "XFS00000123456",
          "10 September 2016").apply(FakeRequest().withJsonBody(Json.parse(callBackResponseJson)))
      status(result) shouldBe OK
    }


    "receive event - return 500 status when a invalid json is received" in new EmailControllerFixture {
      val callBackResponseJson = """{"eventInvalid": [ {"event": "Sent", "detected": "2015-07-02T08:26:39.035Z" }]}"""
      val result: Future[Result] =
        emailController.receiveEmailEvent("API8", "XFS00000123456",
          "10 September 2016").apply(FakeRequest().withJsonBody(Json.parse(callBackResponseJson)))

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "receive event - return 500 status when invalid content Type is received" in new EmailControllerFixture {
      val result: Future[Result] =
        emailController.receiveEmailEvent("API8", "XFS00000123456",
          "10 September 2016").apply(FakeRequest().withTextBody("Error"))

      whenReady(result) {
        result =>
          status(result) shouldBe INTERNAL_SERVER_ERROR
          val document = Jsoup.parse(contentAsString(result))

          document.toString should include("{\"reason\":\"Invalid request content type\"}")
      }
    }
  }

  "EmailController for confirmation" should {

    "return 204 status when the email is sent successfully" in {
      when(mockEmailService.sendConfirmationEmail(any(), any())(any())).thenReturn(Future.successful(EmailResponse(OK, None)))

      val result = emailController.sendConfirmationEmail.apply(FakeRequest().withJsonBody(Json.obj()))
      status(result) shouldBe NO_CONTENT
    }

    "return 400 status when the input json fails validation" in {
      when(mockEmailService.sendConfirmationEmail(any(), any())(any())).thenReturn(Future.successful(EmailResponse(BAD_REQUEST, Some("Bad Thing Happened"))))

      val result = Await.result(emailController.sendConfirmationEmail().apply(FakeRequest().withJsonBody(Json.obj())), 2.second)

      status(result) shouldBe BAD_REQUEST
      val document = Jsoup.parse(contentAsString(result))

      document.toString should include("{\"reason\":\"Bad Thing Happened\"}")
    }

    "receive event - return 200 status when a valid json is received with eventType as delivered " in new EmailControllerFixture {
      val callBackResponseJson = """{"events": [ {"event": "delivered", "detected": "2015-07-02T08:26:39.035Z" }]}"""
      val result: Future[Result] =
        emailController.receiveEmailEvent("API4", "XFS00000123456",
          "10 September 2016").apply(FakeRequest().withJsonBody(Json.parse(callBackResponseJson)))

      status(result) shouldBe OK
    }

    "receive event - return 500 status when a invalid json is received" in new EmailControllerFixture {
      val callBackResponseJson = """{"eventInvalid": [ {"event": "Sent", "detected": "2015-07-02T08:26:39.035Z" }]}"""
      val result: Future[Result] =
        emailController.receiveEmailEvent("API4", "XFS00000123456",
          "10 September 2016").apply(FakeRequest().withJsonBody(Json.parse(callBackResponseJson)))

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "receive event - return 500 status when invalid content Type is received" in new EmailControllerFixture {
      val result: Future[Result] =
        emailController.receiveEmailEvent("API4", "XFS00000123456", "10 September 2016").apply(FakeRequest().withTextBody("You naughty!"))

      whenReady(result) {
        result =>
          status(result) shouldBe INTERNAL_SERVER_ERROR
          val document = Jsoup.parse(contentAsString(result))

          document.toString should include("{\"reason\":\"Invalid request content type\"}")
      }
    }

  }

}
