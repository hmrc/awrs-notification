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

package services

import audit.Auditable
import config.EmailConfig
import connectors.EmailConnector
import models.AwrsValidator._
import models.{ConfirmationEmailRequest, EmailResponse, PushNotificationRequest, SendEmailRequest}
import play.api.Logger
import play.api.i18n.Messages
import play.api.libs.json._
import repositories.ViewedStatus
import uk.gov.hmrc.emailaddress.EmailAddress
import uk.gov.hmrc.play.http._
import utils.ErrorHandling._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait EmailService extends Auditable {

  val emailConnector: EmailConnector

  val cacheService: NotificationCacheService

  lazy val TransactionName = "Send Email Request"

  def sendNotificationEmail(pushNotificationJson: JsValue, registrationNumber: String, host: String)(implicit hc: HeaderCarrier): Future[EmailResponse] =
    Try(pushNotificationJson.as[PushNotificationRequest]) match {
      case Success(notification) =>
        matchTemplateAndRegNumber(notification, registrationNumber, host, sendEmailRequest)

      case Failure(ex: JsResultException) =>
        Logger.warn("[API12] Email service JsResultException: " + ex.errors)
        Future.successful(getValidationError(ex.errors))

      case Failure(e) =>
        Logger.warn("[API12] Email service error: " + e.getMessage)
        Future.successful(EmailResponse(500, Some(e.getMessage)))
    }

  def sendConfirmationEmail(confirmationEmailJson: JsValue, host: String)(implicit hc: HeaderCarrier): Future[EmailResponse] =
    Try(confirmationEmailJson.as[ConfirmationEmailRequest]) match {
      case Success(request) =>

        EmailConfig.getConfirmationTemplate(request) match {
          case Some(templateId) =>
            val parameterMap: Map[String, String] = Map("organisationName" -> confirmationEmailJson.bus, "applicationReference" -> registrationNumber, "submissionDate" -> xxx)

            val emailRequest = SendEmailRequest(to = List(EmailAddress(confirmationEmailJson.email)),
              templateId = templateId,
              parameters = parameterMap,
              force = false,
              Some("http://" + host + controllers.routes.EmailController.receiveEvent(notificationRequest.name, registrationNumber, notificationRequest.email).url))

            sendEmailFun.apply(emailRequest)

          case _ =>
            Logger.warn("[API12] Email service error: " + Messages("template_mapping.error"))
            Future.successful(EmailResponse(503, Some(Messages("template_mapping.error"))))
        }
      case Failure(ex: JsResultException) =>
        Logger.warn("[API12] Email service JsResultException: " + ex.errors)
        Future.successful(getValidationError(ex.errors))

      case Failure(e) =>
        Logger.warn("[API12] Email service error: " + e.getMessage)
        Future.successful(EmailResponse(500, Some(e.getMessage)))
    }

  private def matchTemplateAndRegNumber(notificationRequest: PushNotificationRequest, registrationNumber: String, host: String, sendEmailFun: (SendEmailRequest) => Future[EmailResponse])(implicit hc: HeaderCarrier): Future[EmailResponse] =
    (EmailConfig.getNotificationTemplate(notificationRequest), registrationNumber.matches(registrationRegex)) match {
      case (Some(templateId), true) =>
        // store the notification details in Mongo if the template and reference number are valid
        cacheService.storeNotification(notificationRequest, registrationNumber)
        // make sure the notification status flag is set to false to make sure it is viewed when the user next logs in
        cacheService.storeNotificationViewedStatus(false, registrationNumber)

        val parameterMap: Map[String, String] = Map("name" -> notificationRequest.name, "registrationNumber" -> registrationNumber)

        val emailRequest = SendEmailRequest(to = List(EmailAddress(notificationRequest.email)),
          templateId = templateId,
          parameters = parameterMap,
          force = false,
          Some("http://" + host + controllers.routes.EmailController.receiveEvent(notificationRequest.name, registrationNumber, notificationRequest.email).url))

        val auditMap: Map[String, String] = Map("name" -> notificationRequest.name, "registrationNumber" -> registrationNumber, "emailAddress" -> notificationRequest.email, "status" -> notificationRequest.status.fold("")(x => x), "contactType" -> notificationRequest.contact_type.fold("")(x => x))
        val auditEventType: String = "awrs-notification"
        sendDataEvent(transactionName = TransactionName, detail = auditMap, eventType = auditEventType)

        sendEmailFun.apply(emailRequest)
      case (_, false) =>
        Logger.warn("[API12] Email service error: " + Messages("registration_number.invalid"))
        Future.successful(EmailResponse(400, Some(Messages("registration_number.invalid"))))

      case (_, _) =>
        Logger.warn("[API12] Email service error: " + Messages("template_mapping.error"))
        Future.successful(EmailResponse(503, Some(Messages("template_mapping.error"))))
    }

  private def sendEmailRequest(request: SendEmailRequest)(implicit headerCarrier: HeaderCarrier): Future[EmailResponse] =
    emailConnector.sendEmail(request) map {
      response =>
        response.status match {
          case 202 =>
            Logger.warn(f"[API12] Email with template id: ${request.templateId} was sent successfully")
            EmailResponse(200, None)
          case 400 =>
            Logger.warn("[API12] Email connector returned Bad Request: " + extractResponseMessage(response))
            EmailResponse(500, Some(extractResponseMessage(response)))
          case _ =>
            Logger.warn("[API12] Email connector returned Error Response: " + extractResponseMessage(response))
            EmailResponse(503, Some(extractResponseMessage(response)))
        }
    } recover {
      case e: BadGatewayException =>
        Logger.warn("[API12] Email connector BadGatewayException: " + e.message)
        EmailResponse(503, Some(e.message))
      case e: Exception =>
        Logger.warn("[API12] Email connector Exception: " + e.getMessage)
        EmailResponse(500, Some(e.getMessage))
    }

}

object EmailService extends EmailService {
  override val emailConnector = EmailConnector
  override val cacheService = NotificationCacheService
}
