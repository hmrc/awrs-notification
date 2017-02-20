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

package controllers

import audit.Auditable
import models.{CallBackEventList, EmailResponse}
import play.api.Logger
import play.api.Play._
import play.api.i18n.Messages
import play.api.libs.json.JsValue
import play.api.mvc._
import services.EmailService
import uk.gov.hmrc.play.microservice.controller.BaseController
import utils.JsonConstructor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object EmailController extends EmailController {
  override val emailService = EmailService
}

trait EmailController extends BaseController with Auditable {

  val emailService: EmailService

  def sendNotificationEmail(registrationNumber: String) = Action.async {
    implicit request =>
      def response(requestJson: JsValue) =
        emailService.sendNotificationEmail(requestJson, registrationNumber, request.host) flatMap {
          emailResponse =>
            extractResponse(emailResponse)
        }

      getResponseJson(request, response)
  }

  def sendConfirmationEmail = Action.async {
    implicit request =>
      def response(requestJson: JsValue) =
        emailService.sendConfirmationEmail(requestJson, request.host) flatMap {
          emailResponse =>
            extractResponse(emailResponse)
        }

      getResponseJson(request, response)
  }

  private def extractResponse(emailResponse: EmailResponse): Future[Result] = {
    emailResponse.status match {
      case OK =>
        Future.successful(Ok)
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


  private def getResponseJson(request: Request[AnyContent], responseFun: (JsValue) => Future[Result]): Future[Result] = {
    request.body match {
      case js: AnyContentAsJson =>
        responseFun.apply(js.json)
      case _ =>
        Logger.warn("[API12] Invalid request body type passed to microservice - just JSON accepted")
        Future.successful(InternalServerError(JsonConstructor.constructErrorJson(Messages("content_type.invalid"))))
    }
  }

  def receiveEvent(name: String, registrationNumber: String, emailAddress: String) = Action.async {
    implicit request =>
      def response(requestJson: JsValue) = {
        val auditMap: Map[String, String] = Map("name" -> name, "registrationNumber" -> registrationNumber, "emailAddress" -> emailAddress)
        val auditEventType: String = "awrs-notification"

        Try(requestJson.as[CallBackEventList](CallBackEventList.reader).callBackEvents) match {
          case Success(callbackEventList) =>
            callbackEventList.foreach {
              event =>
                configuration.getString(event.eventType.toLowerCase) match {
                  case Some(_) =>
                    Logger.warn("[API12] Email Callback Event Received: " + event.eventType)
                    sendDataEvent(transactionName = "Email " + event.eventType, detail = auditMap, eventType = auditEventType)
                  case None =>
                    Logger.warn("[API12] No need to audit the Event Received: " + event.eventType)
                }
            }
            Future.successful(Ok)
          case Failure(e) =>
            Future.successful(InternalServerError(JsonConstructor.constructErrorResponse(EmailResponse(500, Some(e.getMessage)))))
        }
      }
      getResponseJson(request, response)
  }

  def receiveConfirmationEvent(apiType: String, organisationName: String, applicationReference: String, emailAddress: String, submissionDate: String) = Action.async {
    implicit request =>
      def response(requestJson: JsValue) = {
        val auditMap: Map[String, String] = Map("apiType" -> apiType,  "organisationName" -> organisationName, "applicationReference" -> applicationReference, "emailAddress" -> emailAddress, "submissionDate" -> submissionDate)
        val auditEventType: String = "awrs-api-confirmation"

        Try(requestJson.as[CallBackEventList](CallBackEventList.reader).callBackEvents) match {
          case Success(callbackEventList) =>
            callbackEventList.foreach {
              event =>
                configuration.getString(event.eventType.toLowerCase) match {
                  case Some(_) =>
                    Logger.warn(s"[API Confirmation] Email Callback Event Received: ${event.eventType}")
                    sendDataEvent(transactionName = "Email " + event.eventType, detail = auditMap, eventType = auditEventType)
                  case None =>
                    Logger.warn(s"[API Confirmation] No need to audit the Event Received: ${event.eventType}")
                }
            }
            Future.successful(Ok)
          case Failure(e) =>
            Future.successful(InternalServerError(JsonConstructor.constructErrorResponse(EmailResponse(500, Some(e.getMessage)))))
        }
      }
      getResponseJson(request, response)
  }
}
