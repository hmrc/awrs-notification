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

import audit.Auditable
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import javax.inject.{Inject, Named}
import models.{CallBackEventList, EmailResponse}
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.mvc._
import services.EmailService
import utils.JsonConstructor
import utils.ErrorNotifications._
import utils.EmailHelper._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

class EmailController @Inject()(val auditConnector: AuditConnector,
                                     val emailService: EmailService,
                                     cc: ControllerComponents,
                                     @Named("appName") val appName: String) extends BackendController(cc) with Logging
                                      with Auditable {

  def sendNotificationEmail(registrationNumber: String): Action[AnyContent] = Action.async {
    implicit request =>
      def response(requestJson: JsValue): Future[Result] =
        emailService.sendNotificationEmail(requestJson, registrationNumber, request.host) flatMap {
          emailResponse =>
            extractResponse(emailResponse)
        }

      getResponseJson(request, response)
  }

  def sendWithdrawnEmail: Action[AnyContent] = Action.async {
    implicit request => sendEmail(request, emailService.sendWithdrawnEmail)
  }

  def sendCancellationEmail: Action[AnyContent] = Action.async {
    implicit request => sendEmail(request, emailService.sendCancellationEmail)
  }

  def sendConfirmationEmail: Action[AnyContent] = Action.async {
    implicit request => sendEmail(request, emailService.sendConfirmationEmail)
  }

  private def sendEmail(request: Request[AnyContent], emailSender: (JsValue, String) =>
    Future[EmailResponse]): Future[Result] = {
    def response(requestJson: JsValue): Future[Result] =
      emailSender(requestJson, request.host) flatMap {
        emailResponse =>
          extractResponse(emailResponse)
      }

    getResponseJson(request, response)
  }

  private def extractResponse(emailResponse: EmailResponse): Future[Result] = {
    emailResponse.status match {
      case OK =>
        Future.successful(NoContent)
      case BAD_REQUEST =>
        Future.successful(BadRequest(JsonConstructor.constructErrorResponse(emailResponse)))
      case NOT_FOUND =>
        Future.successful(NotFound(JsonConstructor.constructErrorResponse(emailResponse)))
      case INTERNAL_SERVER_ERROR =>
        Future.successful(InternalServerError(JsonConstructor.constructErrorResponse(emailResponse)))
      case _ =>
        Future.successful(ServiceUnavailable(JsonConstructor.constructErrorResponse(emailResponse)))
    }
  }


  private def getResponseJson(request: Request[AnyContent], responseFun: JsValue => Future[Result]): Future[Result] = {
    request.body match {
      case js: AnyContentAsJson =>
        responseFun.apply(js.json)
      case _ =>
        logger.warn("[API12] Invalid request body type passed to microservice - just JSON accepted")
        Future.successful(InternalServerError(JsonConstructor.constructErrorJson(invalidContentType)))
    }
  }

  def receiveEvent(name: String, registrationNumber: String): Action[AnyContent] = Action.async {
    implicit request =>
      def response(requestJson: JsValue): Future[Result] = {
        val auditMap: Map[String, String] = Map("name" -> name, "registrationNumber" -> registrationNumber)
        val auditEventType: String = "awrs-notification"
        getEmailEvent(requestJson, auditMap, auditEventType, apiType = "12")
      }

      getResponseJson(request, response)
  }

  def receiveEmailEvent(apiType: String, applicationReference: String, submissionDate: String): Action[AnyContent] = Action.async {
    implicit request =>
      def response(requestJson: JsValue): Future[Result] = {
        val auditMap: Map[String, String] = Map("apiType" -> apiType, "applicationReference" -> applicationReference,
          "submissionDate" -> submissionDate)
        val auditEventType: String = s"awrs-api-${apiType.toLowerCase}"
        getEmailEvent(requestJson, auditMap, auditEventType, apiType)
      }

      getResponseJson(request, response)
  }

  private def getEmailEvent(requestJson: JsValue, auditMap: Map[String, String],
                            auditEventType: String, apiType: String)(implicit hc: HeaderCarrier): Future[Result] = {
    Try(requestJson.as[CallBackEventList](CallBackEventList.reader).callBackEvents) match {
      case Success(callbackEventList) =>
        callbackEventList.foreach {
          event =>
            //configuration.getString(event.eventType.toLowerCase) match {
            callBackEvents(event.eventType.toLowerCase) match {
              case Some(_) =>
                logger.warn(s"[API$apiType] Email Callback Event Received: ${event.eventType}")
                sendDataEvent(transactionName = "Email " + event.eventType, detail = auditMap, eventType = auditEventType)
              case None =>
                logger.warn(s"[API$apiType] No need to audit the Event Received: ${event.eventType}")
            }
        }
        Future.successful(Ok)
      case Failure(e) =>
        Future.successful(InternalServerError(JsonConstructor.constructErrorResponse(EmailResponse(INTERNAL_SERVER_ERROR,
          Some(e.getMessage)))))
    }
  }
}

