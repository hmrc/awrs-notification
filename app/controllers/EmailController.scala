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

import audit.Auditable
import config.AwrsNotificationAuditConnector
import models.{CallBackEventList, EmailResponse}
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.play.audit.model.Audit
import uk.gov.hmrc.play.config.AppName
import utils.JsonConstructor
import services.EmailService
import uk.gov.hmrc.play.microservice.controller.BaseController
import play.api.mvc._

import scala.concurrent.Future
import play.api.Logger

import scala.util.{Failure, Success, Try}
import play.api.Play._
import repositories.NotificationRepository

import scala.concurrent.ExecutionContext.Implicits.global

object EmailController extends EmailController {
  override val emailService = EmailService
  override val notificationRepo: NotificationRepository = NotificationRepository()
}

trait EmailController extends BaseController with Auditable {

  val emailService: EmailService
  val notificationRepo: NotificationRepository

  def sendEmail(registrationNumber: String) = Action.async {
    implicit request =>
      def response(requestJson: JsValue) =
        emailService.sendEmail(requestJson, registrationNumber, request.host) flatMap {
          emailResponse =>
            emailResponse.status match {
              case 200 =>
                Future.successful(Ok)
              case 400 =>
                Future.successful(BadRequest(JsonConstructor.constructErrorResponse(emailResponse)))
              case 404 =>
                Future.successful(NotFound(JsonConstructor.constructErrorResponse(emailResponse)))
              case 500 =>
                Future.successful(InternalServerError(JsonConstructor.constructErrorResponse(emailResponse)))
              case _ =>
                Future.successful(ServiceUnavailable(JsonConstructor.constructErrorResponse(emailResponse)))
            }
        }

      getResponseJson(request, response)
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
}
