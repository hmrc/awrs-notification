/*
 * Copyright 2018 HM Revenue & Customs
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
import models.ViewedStatusResponse
import play.api.libs.json.Json
import play.api.mvc._
import services.NotificationCacheService
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global

object NotificationCacheController extends NotificationCacheController {
  override val notificationService: NotificationCacheService = NotificationCacheService
}

trait NotificationCacheController extends BaseController with Auditable {

  val notificationService: NotificationCacheService

  def getNotification(registrationNumber: String): Action[AnyContent] = Action.async {
    implicit request =>
      notificationService.findNotification(registrationNumber) map {
        case Some(notification) => Ok(Json.toJson(notification))
        case _ => NotFound
      }
  }

  def deleteNotification(registrationNumber: String): Action[AnyContent] = Action.async {
    implicit request =>
      notificationService.deleteNotification(registrationNumber) map {
        case (true, _) => Ok
        case (false, Some(error)) => InternalServerError(error)
        case _ => InternalServerError
      }
  }

  def getNotificationViewedStatus(registrationNumber: String): Action[AnyContent] = Action.async {
    implicit request =>
      notificationService.findNotificationViewedStatus(registrationNumber) map {
        case Some(viewedStatus) => Ok(Json.toJson[ViewedStatusResponse](viewedStatus))
        case _ => Ok(Json.toJson(ViewedStatusResponse(false)))
      }
  }

  def markAsViewed(registrationNumber: String): Action[AnyContent] = Action.async {
    implicit request =>
      notificationService.markAsViewed(registrationNumber) map {
        case (true, _) => Ok
        case (false, Some(error)) => InternalServerError(error)
        case _ => InternalServerError
      }
  }
}
