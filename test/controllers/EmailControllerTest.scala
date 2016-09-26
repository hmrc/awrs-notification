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

package controllers

import audit.TestAudit
import models.EmailResponse
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.NotificationRepository
import services.EmailService
import uk.gov.hmrc.play.audit.model.Audit
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class EmailControllerTest extends UnitSpec with MockitoSugar with ScalaFutures with OneServerPerSuite {

  val mockEmailService = mock[EmailService]
  val mockNotificiationRepo = mock[NotificationRepository]

  val emailController = new EmailController {
    override val emailService = mockEmailService
    override val audit: Audit = new TestAudit
    override val notificationRepo: NotificationRepository = mockNotificiationRepo
  }

  trait EmailControllerFixture {
    val emailController = new EmailController {
      override val emailService = mockEmailService
      override val audit: Audit = new TestAudit
      override val notificationRepo: NotificationRepository = mockNotificiationRepo
    }
  }

  implicit val mockHeaderCarrier: HeaderCarrier = HeaderCarrier()

  "EmailController" should {
    "use the correct Email Service" in {
      EmailController.emailService shouldBe EmailService
    }

    "return 200 status when the email is sent successfully" in {
      when(mockEmailService.sendEmail(any(), any(), any())(any())).thenReturn(Future.successful(EmailResponse(200, None)))

      val result = emailController.sendEmail("").apply(FakeRequest().withJsonBody(Json.obj()))
      status(result) shouldBe OK
    }

    "return 400 status when the input json fails validation" in {
      when(mockEmailService.sendEmail(any(), any(), any())(any())).thenReturn(Future.successful(EmailResponse(400, Some("Bad Thing Happened"))))

      val result = Await.result(emailController.sendEmail("").apply(FakeRequest().withJsonBody(Json.obj())), 2.second)

      status(result) shouldBe BAD_REQUEST
      jsonBodyOf(result).toString shouldBe "{\"reason\":\"Bad Thing Happened\"}"
    }

    "return 400 status when the input json fails validation (empty response body)" in {
      when(mockEmailService.sendEmail(any(), any(), any())(any())).thenReturn(Future.successful(EmailResponse(400, Some(""))))

      val result = Await.result(emailController.sendEmail("").apply(FakeRequest().withJsonBody(Json.obj())), 2.second)

      status(result) shouldBe BAD_REQUEST
      jsonBodyOf(result).toString shouldBe "{\"reason\":\"Unknown reason. Dependent system did not provide failure reason\"}"
    }

    "return 400 status when the input json fails validation (no errors)" in {
      when(mockEmailService.sendEmail(any(), any(), any())(any())).thenReturn(Future.successful(EmailResponse(400, None)))

      val result = Await.result(emailController.sendEmail("").apply(FakeRequest().withJsonBody(Json.obj())), 2.second)

      status(result) shouldBe BAD_REQUEST
      jsonBodyOf(result).toString shouldBe "{\"reason\":\"Unknown reason. Dependent system did not provide failure reason\"}"
    }

    "return 404 status when the email template is not found" in {
      when(mockEmailService.sendEmail(any(), any(), any())(any())).thenReturn(Future.successful(EmailResponse(404, Some("Invalid template"))))

      val result = Await.result(emailController.sendEmail("").apply(FakeRequest().withJsonBody(Json.obj())), 2.second)

      status(result) shouldBe NOT_FOUND
      jsonBodyOf(result).toString shouldBe "{\"reason\":\"Invalid template\"}"
    }

    "return 500 status when the email connector fails for external reasons (template not found or validation error occurred in external Email service)" in {
      when(mockEmailService.sendEmail(any(), any(), any())(any())).thenReturn(Future.successful(EmailResponse(500, Some("Invalid template"))))

      val result = Await.result(emailController.sendEmail("").apply(FakeRequest().withJsonBody(Json.obj())), 2.second)

      status(result) shouldBe INTERNAL_SERVER_ERROR
      jsonBodyOf(result).toString shouldBe "{\"reason\":\"Invalid template\"}"
    }

    "return 500 status when the request is of wrong type" in {
      val result = Await.result(emailController.sendEmail("").apply(FakeRequest().withTextBody("TEXT")), 2.second)

      status(result) shouldBe INTERNAL_SERVER_ERROR
      jsonBodyOf(result).toString shouldBe "{\"reason\":\"Invalid request content type\"}"
    }

    "return 503 status when email connector fails for external reasons" in {
      when(mockEmailService.sendEmail(any(), any(), any())(any())).thenReturn(Future.successful(EmailResponse(503, Some("Something Bad Happened"))))

      val result = Await.result(emailController.sendEmail("").apply(FakeRequest().withJsonBody(Json.obj())), 2.second)

      status(result) shouldBe SERVICE_UNAVAILABLE
      jsonBodyOf(result).toString shouldBe "{\"reason\":\"Something Bad Happened\"}"
    }

    "return 503 status when email connector fails for external reasons (empty response)" in {
      when(mockEmailService.sendEmail(any(), any(), any())(any())).thenReturn(Future.successful(EmailResponse(503, Some(""))))

      val result = Await.result(emailController.sendEmail("").apply(FakeRequest().withJsonBody(Json.obj())), 2.second)

      status(result) shouldBe SERVICE_UNAVAILABLE
      jsonBodyOf(result).toString shouldBe "{\"reason\":\"Unknown reason. Dependent system did not provide failure reason\"}"
    }

    "receive event - return 200 status when a valid json is received with eventType as delivered " in new EmailControllerFixture {
      val callBackResponseJson = """{"events": [ {"event": "delivered", "detected": "2015-07-02T08:26:39.035Z" }]}"""
      val result = emailController.receiveEvent("firstName", "XFS00000123456", "example@example.com").apply(FakeRequest().withJsonBody(Json.parse(callBackResponseJson)))

      status(result) shouldBe OK
    }

    "receive event - return 200 status when a valid json is received with eventType as permanentBounce" in new EmailControllerFixture {
      val callBackResponseJson = """{"events": [ {"event": "permanentBounce", "detected": "2015-07-02T08:26:39.035Z" }]}"""
      val result = emailController.receiveEvent("firstName", "XFS00000123456", "example@example.com").apply(FakeRequest().withJsonBody(Json.parse(callBackResponseJson)))

      status(result) shouldBe OK
    }

    "receive event - return 200 status when a valid json is received with eventType as Sent" in new EmailControllerFixture {
      val callBackResponseJson = """{"events": [ {"event": "Sent", "detected": "2015-07-02T08:26:39.035Z" }]}"""
      val result = emailController.receiveEvent("firstName", "XFS00000123456", "example@example.com").apply(FakeRequest().withJsonBody(Json.parse(callBackResponseJson)))

      status(result) shouldBe OK
    }

    "receive event - return 500 status when a invalid json is received" in new EmailControllerFixture {
      val callBackResponseJson = """{"eventInvalid": [ {"event": "Sent", "detected": "2015-07-02T08:26:39.035Z" }]}"""
      val result = emailController.receiveEvent("firstName", "XFS00000123456", "example@example.com").apply(FakeRequest().withJsonBody(Json.parse(callBackResponseJson)))

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "receive event - return 500 status when invalid content Type is received" in new EmailControllerFixture {
      val result = emailController.receiveEvent("firstName", "XFS00000123456", "example@example.com").apply(FakeRequest().withTextBody("You naughty!"))

      whenReady(result) {
        result =>
          status(result) shouldBe INTERNAL_SERVER_ERROR
          jsonBodyOf(result).toString shouldBe "{\"reason\":\"Invalid request content type\"}"
      }
    }
  }
}