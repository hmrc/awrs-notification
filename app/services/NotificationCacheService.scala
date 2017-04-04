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

import models.{PushNotificationRequest}
import repositories.{NotificationRepository, NotificationViewedRepository, StatusNotification, ViewedStatus}
import models.ContactTypes._
import org.joda.time.{LocalDateTime}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import uk.gov.hmrc.play.http.HeaderCarrier

import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait NotificationCacheService {
  val repository: NotificationRepository
  val viewedRepository: NotificationViewedRepository
  val dateFormat: String = "yyyy-MM-dd'T'HH:mm:ss"
  val fmt: DateTimeFormatter= DateTimeFormat.forPattern(dateFormat)

  def storeNotification(pushNotification: PushNotificationRequest, registrationNumber: String)(implicit hc: HeaderCarrier): Future[Boolean] = {

    val notification = StatusNotification(registrationNumber = Some(registrationNumber),
      contactNumber = pushNotification.contact_number,
      contactType = pushNotification.contact_type,
      status = pushNotification.status,
      storageDatetime =  Some(fmt.print(new LocalDateTime()))
    )
    // only store for the 'minded to' and 'not minded to' contact types
    pushNotification.contact_type match {
      case Some(MTRJ | MTRV | NMRV | REJR | REVR) => repository.insertStatusNotification(notification)
      case _ => Future.successful(false)
    }
  }

  def findNotification(registrationNumber: String)(implicit hc: HeaderCarrier): Future[Option[StatusNotification]] = {

    repository.findByRegistrationNumber(registrationNumber) map {
      case notification@Some(x) => x.contactType match {
        case Some(MTRJ | MTRV | NMRV | REJR | REVR) => notification
        case _ => None
      }
      case _ => None
    }
  }

  def deleteNotification(registrationNumber: String)(implicit hc: HeaderCarrier): Future[(Boolean, Option[String])] =
    repository.deleteStatusNotification(registrationNumber) map {
      result =>
        result.ok match {
          case true => (true, None)
          case false => (false, result.errmsg)
        }
    }

  def storeNotificationViewedStatus(viewedStatus: Boolean, registrationNumber: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    val status = ViewedStatus(registrationNumber = Some(registrationNumber), viewed = Some(viewedStatus))
    viewedRepository.insertViewedStatus(status)
  }

  def findNotificationViewedStatus(registrationNumber: String)(implicit hc: HeaderCarrier): Future[Option[ViewedStatus]] =
    viewedRepository.findViewedStatusByRegistrationNumber(registrationNumber) map {
      case viewedStatus@Some(_) => viewedStatus
      case _ => None
    }

  def markAsViewed(registrationNumber: String)(implicit hc: HeaderCarrier): Future[(Boolean, Option[String])] =
    viewedRepository.markAsViewed(registrationNumber) map {
      result =>
        result.ok match {
          case true => (true, None)
          case false => (false, result.errmsg)
        }
    }

}

object NotificationCacheService extends NotificationCacheService {
  override val repository = NotificationRepository()
  override val viewedRepository = NotificationViewedRepository()
}
