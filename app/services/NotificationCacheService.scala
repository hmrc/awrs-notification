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

import models.PushNotificationRequest
import repositories.{NotificationRepository, StatusNotification}
import models.ContactTypes._
import uk.gov.hmrc.play.http.HeaderCarrier

import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait NotificationCacheService {

  val repository: NotificationRepository

  def storeNotification(pushNotification: PushNotificationRequest, registrationNumber: String)(implicit hc: HeaderCarrier): Future[Boolean] = {

    val notification = StatusNotification(registrationNumber = Some(registrationNumber),
      contactNumber = pushNotification.contact_number,
      contactType = pushNotification.contact_type,
      status = pushNotification.status
    )
    // only store for the 'minded to' and 'not minded to' contact types
    pushNotification.contact_type match {
      case Some(MTRJ | MTRV | NMRV) => repository.insertStatusNotification(notification)
      case _ => Future.successful(false)
    }
  }

  def findNotification(registrationNumber: String)(implicit hc: HeaderCarrier): Future[Option[StatusNotification]] =
    repository.findByRegistrationNumber(registrationNumber) map {
      case notification@Some(x) => x.contactType match {
        case Some(MTRJ | MTRV | NMRV) => notification
        case _ => None
      }
      case _ => None
    }

  def deleteNotification(registrationNumber: String)(implicit hc: HeaderCarrier): Future[(Boolean, Option[String])] =
    repository.deleteStatusNotification(registrationNumber) map {
      result =>
        result.ok match {
          case true => (true, None)
          case false => (false, result.errmsg)
        }
    }

}

object NotificationCacheService extends NotificationCacheService {
  override val repository = NotificationRepository()
}