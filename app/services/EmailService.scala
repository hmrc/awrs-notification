/*
 * Copyright 2017 HM Revenue & Customs
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
import config.{EmailConfig, ErrorConfig}
import connectors.EmailConnector
import models.AwrsValidator._
import models.{EmailRequest, EmailResponse, PushNotificationRequest, SendEmailRequest}
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json._
import uk.gov.hmrc.emailaddress.EmailAddress
import uk.gov.hmrc.play.http._
import utils.ErrorHandling._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import uk.gov.hmrc.http.{ BadGatewayException, HeaderCarrier }

trait EmailService extends Auditable {

  val emailConnector: EmailConnector

  val cacheService: NotificationCacheService

  lazy val TransactionName = "Send Email Request"

  def sendNotificationEmail(pushNotificationJson: JsValue, registrationNumber: String, host: String)(implicit hc: HeaderCarrier): Future[EmailResponse] =
    Try(pushNotificationJson.as[PushNotificationRequest]) match {
      case Success(notification) =>
        matchTemplateAndRegNumber(notification, registrationNumber, host)

      case Failure(ex: JsResultException) =>
        Logger.warn("[API12] Email service JsResultException: " + ex.errors)
        Future.successful(getValidationError(ex.errors))

      case Failure(e) =>
        Logger.warn("[API12] Email service error: " + e.getMessage)
        Future.successful(EmailResponse(500, Some(e.getMessage)))
    }

  private[services] def now(): String = DateTime.now.toString("dd MMMM yyyy")

  def sendWithdrawnEmail(withdrawnEmailJson: JsValue, host: String)(implicit hc: HeaderCarrier): Future[EmailResponse] = {
    SendEmail(withdrawnEmailJson, host, EmailConfig.getWithdrawnTemplate, "Withdrawl")
  }

  def sendCancellationEmail(cancellationEmailJson: JsValue, host: String)(implicit hc: HeaderCarrier): Future[EmailResponse] = {
    SendEmail(cancellationEmailJson, host, EmailConfig.getCancellationTemplate, "Cancellation")
  }

  def sendConfirmationEmail(confirmationEmailJson: JsValue, host: String)(implicit hc: HeaderCarrier): Future[EmailResponse] = {
    SendEmail(confirmationEmailJson, host, EmailConfig.getConfirmationTemplate, "Confirmation")
  }

  private def SendEmail(email: JsValue, host: String,
                        getEmailTemplate: (EmailRequest) => Option[String],
                        action: String)(implicit hc: HeaderCarrier) = {
    Try(email.as[EmailRequest]) match {
      case Success(request) =>
        val submissionDate = now()
        getEmailTemplate(request) match {
          case Some(templateId) =>

            val reference = request.reference.getOrElse("null")
            val deregistrationDateStr = request.deregistrationDateStr.getOrElse("")
            val parameterMap: Map[String, String] = Map("organisationName" -> request.businessName,
              "applicationReference" -> reference,
              "submissionDate" -> submissionDate,
              "deregistrationDate" -> deregistrationDateStr)

            val emailRequest = SendEmailRequest(to = List(EmailAddress(request.email)),
              templateId = templateId,
              parameters = parameterMap,
              force = false,
              eventUrl = Some("http://" + host + controllers.routes.EmailController.receiveEmailEvent(request.apiType.toString,
                reference, submissionDate).url))

            val auditMap: Map[String, String] = Map("apiType" -> request.apiType,
              "organisationName" -> request.businessName,
              "applicationReference" -> reference,
              "emailAddress" -> request.email,
              "submissionDate" -> submissionDate)
            val auditEventType: String = s"awrs-api-${action.toLowerCase}"
            sendDataEvent(transactionName = TransactionName, detail = auditMap, eventType = auditEventType)

            sendEmailRequest(logName = s"API ${action}", emailRequest)

          case _ =>
            Logger.warn(s"[API ${action}] Email service error: " + ErrorConfig.invalidTemplate)
            Future.successful(EmailResponse(503, Some(ErrorConfig.invalidTemplate)))
        }
      case Failure(ex: JsResultException) =>
        Logger.warn(s"[API ${action}] Email service JsResultException: " + ex.errors)
        Future.successful(getValidationError(ex.errors))

      case Failure(e) =>
        Logger.warn(s"[API ${action}] Email service error: " + e.getMessage)
        Future.successful(EmailResponse(500, Some(e.getMessage)))
    }
  }

  private def matchTemplateAndRegNumber(notificationRequest: PushNotificationRequest,
                                        registrationNumber: String, host: String)
                                       (implicit hc: HeaderCarrier): Future[EmailResponse] =
    (EmailConfig.getNotificationTemplate(notificationRequest), registrationNumber.matches(registrationRegex)) match {
      case (Some(templateId), true) =>
        // store the notification details in Mongo if the template and reference number are valid
        cacheService.storeNotification(notificationRequest, registrationNumber)
        // make sure the notification status flag is set to false to make sure it is viewed when the user next logs in
        cacheService.storeNotificationViewedStatus(viewedStatus = false, registrationNumber)

        val parameterMap: Map[String, String] = Map("name" -> notificationRequest.name,
          "registrationNumber" -> registrationNumber)

        val emailRequest = SendEmailRequest(to = List(EmailAddress(notificationRequest.email)),
          templateId = templateId,
          parameters = parameterMap,
          force = false,
          Some("http://" + host + controllers.routes.EmailController.receiveEvent(notificationRequest.name,registrationNumber).url))

        val auditMap: Map[String, String] = Map("name" -> notificationRequest.name,
          "registrationNumber" -> registrationNumber,
          "emailAddress" -> notificationRequest.email,
          "status" -> notificationRequest.status.fold("")(x => x),
          "contactType" -> notificationRequest.contact_type.fold("")(x => x))
        val auditEventType: String = "awrs-notification"
        sendDataEvent(transactionName = TransactionName, detail = auditMap, eventType = auditEventType)

        sendEmailRequest(logName = "API12", emailRequest)
      case (_, false) =>
        Logger.warn("[API12] Email service error: " + ErrorConfig.invalidRegNumber)
        Future.successful(EmailResponse(400, Some(ErrorConfig.invalidRegNumber)))

      case (_, _) =>
        Logger.warn("[API12] Email service error: " + ErrorConfig.invalidTemplate)
        Future.successful(EmailResponse(503, Some(ErrorConfig.invalidTemplate)))
    }

  private def sendEmailRequest(logName: String, request: SendEmailRequest)(implicit headerCarrier: HeaderCarrier): Future[EmailResponse] =
    emailConnector.sendEmail(request) map {
      response =>
        response.status match {
          case 202 =>
            Logger.warn(s"[$logName] Email with template id: ${request.templateId} was sent successfully")
            EmailResponse(200, None)
          case 400 =>
            Logger.warn(s"[$logName] Email connector returned Bad Request: " + extractResponseMessage(response))
            EmailResponse(500, Some(extractResponseMessage(response)))
          case _ =>
            Logger.warn(s"[$logName] Email connector returned Error Response: " + extractResponseMessage(response))
            EmailResponse(503, Some(extractResponseMessage(response)))
        }
    } recover {
      case e: BadGatewayException =>
        Logger.warn(s"[$logName] Email connector BadGatewayException: " + e.message)
        EmailResponse(503, Some(e.message))
      case e: Exception =>
        Logger.warn(s"[$logName] Email connector Exception: " + e.getMessage)
        EmailResponse(500, Some(e.getMessage))
    }


}

object EmailService extends EmailService {
  override val emailConnector = EmailConnector
  override val cacheService = NotificationCacheService
}
