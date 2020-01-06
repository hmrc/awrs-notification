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

package services

import audit.TestAudit
import connectors.EmailConnector
import models.{ApiTypes, AwrsValidator, EmailRequest, EmailResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{BadGatewayException, HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.Audit
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.test.UnitSpec
import utils.ErrorNotifications._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class EmailServiceTest extends UnitSpec with MockitoSugar with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val mockEmailConnector: EmailConnector = mock[EmailConnector]
  val mockNotificiationCacheService: NotificationCacheService = mock[NotificationCacheService]

  val mockAuditConnector: AuditConnector = mock[AuditConnector]
  val config: ServicesConfig = app.injector.instanceOf[ServicesConfig]

  val emailService: EmailService = new EmailService(mockAuditConnector, mockEmailConnector, mockNotificiationCacheService, config, "awrs-notification") {
    override val audit: Audit = new TestAudit(mockAuditConnector)
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockEmailConnector)
    reset(mockNotificiationCacheService)
  }

  implicit val mockHeaderCarrier: HeaderCarrier = HeaderCarrier()

  def acceptedMock: OngoingStubbing[Future[HttpResponse]] =
    when(emailService.emailConnector.sendEmail(any())(any())).thenReturn(Future.successful(HttpResponse(202)))

  "EmailService for notification" should {

    "return 200 status when the email is sent successfully" in {
      val inputJson =
        """
          |{"name": "name", "email": "example@example.com", "status": "04", "contact_type": "REJR", "contact_number": "123456789012", "variation": false}
          |""".stripMargin

      acceptedMock

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 200
    }

    "return 200 status when the email is sent successfully (140 char name)" in {
      val longName = "a" * AwrsValidator.maxTextLength
      val inputJson =
        s"""
           |{"name": "$longName", "email": "example@example.com", "status": "04", "contact_type": "REJR", "contact_number": "123456789012", "variation": false}
           |""".stripMargin

      acceptedMock

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 200
    }

    "return 200 status when the email is sent successfully (REVR)" in {
      val inputJson =
        """
          |{"name": "name", "email": "example@example.com", "status": "04", "contact_type": "REVR", "contact_number": "123456789012", "variation": false}
          |""".stripMargin

      acceptedMock

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson), "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 200
    }

    "return 200 status when the email is sent successfully (CONA)" in {
      val inputJson =
        """
          |{"name": "name", "email": "example@example.com", "status": "04", "contact_type": "CONA", "contact_number": "123456789012", "variation": false}
          |""".stripMargin

      acceptedMock

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 200
    }

    "return 200 status when the email is sent successfully (MTRJ)" in {
      val inputJson =
        """
          |{"name": "name", "email": "example@example.com", "status": "04", "contact_type": "MTRJ", "contact_number": "123456789012", "variation": false}
          |""".stripMargin

      acceptedMock

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 200
    }

    "return 200 status when the email is sent successfully (NMRJ)" in {
      val inputJson =
        """
          |{"name": "name", "email": "example@example.com", "status": "04", "contact_type": "NMRJ", "contact_number": "123456789012", "variation": false}
          |""".stripMargin

      acceptedMock

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 200
    }

    "return 200 status when the email is sent successfully (MTRV)" in {
      val inputJson =
        """
          |{"name": "name", "email": "example@example.com", "status": "04", "contact_type": "MTRV", "contact_number": "123456789012", "variation": false}
          |""".stripMargin

      acceptedMock

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 200
    }

    "return 200 status when the email is sent successfully (NMRV)" in {
      val inputJson =
        """
          |{"name": "name", "email": "example@example.com", "status": "04", "contact_type": "NMRV", "contact_number": "123456789012", "variation": false}
          |""".stripMargin

      acceptedMock

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson), "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 200
    }

    "return 200 status when the email is sent successfully (OTHR)" in {
      val inputJson =
        """
          |{"name": "name", "email": "example@example.com", "status": "04", "contact_type": "OTHR", "contact_number": "123456789012", "variation": false}
          |""".stripMargin

      acceptedMock

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 200
    }

    "return success when the contact Type is not provided in the request, as it is defaulted to REJR contact type" in {
      val inputJson = """{"name": "name", "email": "example@example.com", "status": "04", "contact_number": "123456789012", "variation": false}"""

      acceptedMock

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 200
    }

    "return success when the status is not provided in the request, as it is an optional field" in {
      val inputJson =
        """
          |{"name": "name", "email": "example@example.com", "contact_type": "REJR", "contact_number": "123456789012", "variation": false}
          |""".stripMargin

      acceptedMock

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 200
    }

    "return success when contact number is not provided in the request, as it is an optional field" in {
      val inputJson = """{"name": "name", "email": "example@example.com", "status": "04", "contact_type": "REJR", "variation": false}"""

      acceptedMock

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 200
    }

    "return 500 status with the failure message when email connector returns Bad Request with content type JSON" in {
      val inputJson =
        """
          |{"name": "name", "email": "example@example.com", "status": "04", "contact_type": "REJR", "contact_number": "123456789012", "variation": false}
          |""".stripMargin

      when(emailService.emailConnector.sendEmail(any())(any())).thenReturn(Future.successful(HttpResponse(400,
        responseHeaders = Map("Content-Type" -> Seq("application/json")),
        responseJson = Some(Json.parse("{\"statusCode\": 400, \"message\": \"Template test does not exist\"}")))))

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 500
      result.errors.get should include("Template test does not exist")
    }

    "return 500 status with the failure message when email connector returns Bad Request with content type JSON (malformed json)" in {
      val inputJson = """{"name": "name", "email": "example@example.com", "status": "04", "contact_type": "REJR", "contact_number": "123456789012", "variation": false}"""

      when(emailService.emailConnector.sendEmail(any())(any())).thenReturn(Future.successful(HttpResponse(400,
        responseHeaders = Map("Content-Type" -> Seq("application/json")), responseJson = Some(Json.parse("{\"hmm\": false}")))))

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 500
      result.errors.get shouldBe "{\"hmm\":false}"
    }

    "return 500 status with the failure message when email connector returns Bad Request with plain text body" in {
      val inputJson =
        """
          |{"name": "name", "email": "example@example.com", "status": "04", "contact_type": "REJR", "contact_number": "123456789012", "variation": false}
          |""".stripMargin

      when(emailService.emailConnector.sendEmail(any())(any())).thenReturn(Future.successful(HttpResponse(400,
        responseHeaders = Map("Content-Type" -> Seq("text/plain")), responseString = Some("Validation Error"))))

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 500
      result.errors.get shouldBe "Validation Error"
    }

    "return 500 status with the failure message when email connector returns Bad Request with plain text body (empty body)" in {
      val inputJson =
        """
          |{"name": "name", "email": "name@example.com", "status": "04", "contact_type": "REJR", "contact_number": "123456789012", "variation": false}
          |""".stripMargin

      when(emailService.emailConnector.sendEmail(any())(any())).thenReturn(Future.successful(HttpResponse(400,
        responseHeaders = Map("Content-Type" -> Seq("text/plain")), responseString = Some(""))))

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 500
      result.errors.get shouldBe ""
    }

    "return 503 status with the failure message when email connector returns anything than 202 or 400 Status" in {
      val inputJson = """{"name": "name", "email": "name@example.com", "status": "04", "contact_type": "REJR", "contact_number": "123456789012", "variation": false}"""

      when(emailService.emailConnector.sendEmail(any())(any())).thenReturn(Future.successful(HttpResponse(500)))

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson), "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 503
    }

    "return 503 status with the failure message when the dependent email service is down" in {
      val inputJson =
        """
          |{"name": "name", "email": "example@example.com", "status": "04", "contact_type": "REJR", "contact_number": "123456789012", "variation": false}
          |""".stripMargin

      when(emailService.emailConnector.sendEmail(any())(any())).thenReturn(
        Future.failed(new BadGatewayException("POST of 'http://localhost:8300/send-templated-email' failed")))

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 503
    }

    "return 503 status with the failure message when unspecified exception occurred in the Email service" in {
      val inputJson =
        """
          |{"name": "name", "email": "example@example.com", "status": "04", "contact_type": "REJR", "contact_number": "123456789012", "variation": false}
          |""".stripMargin

      when(emailService.emailConnector.sendEmail(any())(any())).thenReturn(Future.failed(new Exception("Exception Occurred")))

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 500
    }

    "return 400 status with the failure message when registration number does not mach regex" in {
      val inputJson =
        """
          |{"name": "name", "email": "example@example.com", "status": "04", "contact_type": "REJR", "contact_number": "123456789012", "variation": false}
          |""".stripMargin

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "fds", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 400
      result.errors.get shouldBe invalidRegNumber
    }

    "return 400 status with the failure message when invalid status is passed" in {
      val inputJson =
        """
          |{"name": "name", "email": "example@example.com", "status": "0sad", "contact_type": "REJR", "contact_number": "123456789012", "variation": false}
          |""".stripMargin

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 400
      result.errors.get shouldBe invalidStatus
    }

    "return 400 status with the failure message when invalid variation is passed" in {
      val inputJson =
        """
          |{"name": "name", "email": "name@example.com", "status": "0sad", "contact_type": "REJR", "contact_number": "123456789012", "variation": "false"}
          |""".stripMargin

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 400
      result.errors.get shouldBe errorExpectedBoolean
    }

    "return 400 status with the failure message when invalid email address is passed" in {
      val inputJson = """{"name": "name", "email": "exampleexample.com", "status": "06", "contact_type": "REJR", "contact_number": "123456789012", "variation": false}"""

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson), "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 400
      result.errors.get shouldBe invalidEmail
    }

    "return 400 status with the failure message when invalid name is passed (max length exceeded)" in {
      val longName = "a" * (AwrsValidator.maxTextLength + 1)
      val inputJson = s"""{"name": "$longName", "email": "example@example.com", "status": "04", "contact_type": "REJR", "contact_number": "123456789012", "variation": false}"""

      acceptedMock

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson), "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 400
      result.errors.get shouldBe invalidName
    }

    "return 400 status with the failure message when invalid contact number is passed" in {
      val inputJson =
        """
          |{"name": "name", "email": "example@example.com", "status": "05", "contact_type": "REJR", "contact_number": "123456789012666666666666", "variation": false}
          |""".stripMargin

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 400
      result.errors.get shouldBe invalidContactNumber
    }

    "return 400 status with the failure message when invalid contact type is passed" in {
      val inputJson =
        """
          |{"name": "name", "email": "example@example.com", "status": "05", "contact_type": "XXXXASDADJSA:OJD", "contact_number": "1234567890125", "variation": false}
          |""".stripMargin

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 400
      result.errors.get shouldBe invalidContactType
    }

    "return 400 status with the failure message when Email is more than 100 characters" in {
      val inputJson =
        """
          |{"name": "name", "email": "example.example.example.example.example.example.example.example.example.example.example.example.example@example.com",
          |"status": "05", "contact_type": "REJR", "contact_number": "123456789012", "variation": false}""".stripMargin

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 400
      result.errors.get shouldBe errorMaxLength
    }

    "return 400 status when the email has unknown characters" in {
      val inputJson =
        """
          |{"name": "name", "email": "example%@example.com", "status": "04", "contact_type": "REJR", "contact_number": "123456789012", "variation": false}
          |""".stripMargin

      acceptedMock

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 400
      result.errors.get shouldBe invalidEmail
    }

    "return 400 status when the name has unknown characters" in {
      val inputJson =
        """
          |{"name": "name£«æ…≥≤", "email": "example@example.com", "status": "04", "contact_type": "REJR", "contact_number": "123456789012", "variation": false}
          |""".stripMargin

      acceptedMock

      val result: EmailResponse = Await.result(emailService.sendNotificationEmail(Json.parse(inputJson),
        "XFAW00000123456", "")(hc = mockHeaderCarrier), 2.second)

      result.status shouldBe 400
      result.errors.get shouldBe invalidName
    }

  }

  "EmailService for confirmation" should {

    "check format for now" in {
      val re = "^(([0-9])|([0-2][0-9])|([3][0-1])) (January|February|March|April|May|June|July|August|September|October|November|December) \\d{4}$".r
      emailService.now() should fullyMatch regex re
    }

    val testEmailRequest: JsValue = Json.toJson(EmailRequest(ApiTypes.API4, businessName = "businessName", email = "example@example.com",
      reference = Some("reference"), isNewBusiness = Some(true)))

    "return 200 status when the email is sent successfully" in {
      acceptedMock

      val result: EmailResponse = await(emailService.sendConfirmationEmail(testEmailRequest, host = "")(hc = mockHeaderCarrier))

      result.status shouldBe 200
    }

    "return 500 status when calls to send the email is unsuccessful" in {
      when(emailService.emailConnector.sendEmail(any())(any())).thenReturn(Future.successful(HttpResponse(400)))

      val result: EmailResponse = await(emailService.sendConfirmationEmail(testEmailRequest, host = "")(hc = mockHeaderCarrier))

      result.status shouldBe 500
    }

    "return appropriate status when the input email json is corrupt" in {
      val result: EmailResponse = await(emailService.sendConfirmationEmail(Json.parse("{}"), host = "")(hc = mockHeaderCarrier))

      result.status shouldBe 400
    }
  }

  "EmailService for cancellation" should {
    val testEmailRequest: JsValue = Json.toJson(EmailRequest(ApiTypes.API10, businessName = "businessName",
      email = "example@example.com", deregistrationDateStr = Some("12-07-2019")))

    "return 200 status when the email is sent successfully" in {
      acceptedMock

      val result: EmailResponse = await(emailService.sendCancellationEmail(testEmailRequest, host = "")(hc = mockHeaderCarrier))

      result.status shouldBe 200
    }

    "return 500 status when calls to send the email is unsuccessful" in {
      when(emailService.emailConnector.sendEmail(any())(any())).thenReturn(Future.successful(HttpResponse(400)))

      val result: EmailResponse = await(emailService.sendCancellationEmail(testEmailRequest, host = "")(hc = mockHeaderCarrier))

      result.status shouldBe 500
    }

    "return appropriate status when the input email json is corrupt" in {
      val result: EmailResponse = await(emailService.sendCancellationEmail(Json.parse("{}"), host = "")(hc = mockHeaderCarrier))

      result.status shouldBe 400
    }
  }

  "EmailService for withdrawal" should {
    val testEmailRequest: JsValue = Json.toJson(EmailRequest(ApiTypes.API8, businessName = "businessName", email = "example@example.com"))

    "return 200 status when the email is sent successfully" in {
      acceptedMock

      val result: EmailResponse = await(emailService.sendWithdrawnEmail(testEmailRequest, host = "")(hc = mockHeaderCarrier))

      result.status shouldBe 200
    }

    "return 500 status when calls to send the email is unsuccessful" in {
      when(emailService.emailConnector.sendEmail(any())(any())).thenReturn(Future.successful(HttpResponse(400)))

      val result: EmailResponse = await(emailService.sendWithdrawnEmail(testEmailRequest, host = "")(hc = mockHeaderCarrier))

      result.status shouldBe 500
    }

    "return appropriate status when the input email json is corrupt" in {
      val result: EmailResponse = await(emailService.sendWithdrawnEmail(Json.parse("{}"), host = "")(hc = mockHeaderCarrier))

      result.status shouldBe 400
    }
  }

}

