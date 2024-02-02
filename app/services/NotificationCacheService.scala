/*
 * Copyright 2023 HM Revenue & Customs
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
import models.ContactTypes._
import models.PushNotificationRequest
import java.time.LocalDateTime
import repositories.{NotificationRepository, NotificationViewedRepository, StatusNotification, ViewedStatus}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class NotificationCacheService @Inject()(val auditConnector: AuditConnector,
                                         val repository: NotificationRepository,
                                         val viewedRepository: NotificationViewedRepository,
                                         @Named("appName") val appName: String)(implicit ec: ExecutionContext) extends Auditable  {

  val dateFormat: String = "yyyy-MM-dd'T'HH:mm:ss"
  val fmt: DateTimeFormatter= DateTimeFormatter.ofPattern(dateFormat)

  def storeNotification(pushNotification: PushNotificationRequest,
                        registrationNumber: String): Future[Boolean] = {

    val notification = StatusNotification(registrationNumber = Some(registrationNumber),
      contactNumber = pushNotification.contact_number,
      contactType = pushNotification.contact_type,
      status = pushNotification.status,
      storageDatetime = Some(LocalDateTime.now().format(fmt))
    )
    // only store for the 'minded to' and 'not minded to' contact types
    pushNotification.contact_type match {
      case Some(MTRJ | MTRV | NMRV | REJR | REVR) => repository.insertStatusNotification(notification)
      case _ => Future.successful(false)
    }
  }

  def findNotification(registrationNumber: String): Future[Option[StatusNotification]] = {

    repository.findByRegistrationNumber(registrationNumber) map {
      case notification@Some(x) => x.contactType match {
        case Some(MTRJ | MTRV | NMRV | REJR | REVR) => notification
        case _ => None
      }
      case _ => None
    }
  }

  def deleteNotification(registrationNumber: String): Future[Boolean] =
    repository.deleteStatusNotification(registrationNumber) map {
      result =>
        result.wasAcknowledged()
    }

  def storeNotificationViewedStatus(viewedStatus: Boolean, registrationNumber: String): Future[Boolean] = {
    val status = ViewedStatus(registrationNumber = Some(registrationNumber), viewed = Some(viewedStatus))
    viewedRepository.insertViewedStatus(status)
  }

  def findNotificationViewedStatus(registrationNumber: String): Future[Option[ViewedStatus]] =
    viewedRepository.findViewedStatusByRegistrationNumber(registrationNumber) map {
      case viewedStatus@Some(_) => viewedStatus
      case _ => None
    }

  def markAsViewed(registrationNumber: String): Future[Boolean] =
    viewedRepository.markAsViewed(registrationNumber) map {
      result =>
        result.wasAcknowledged()
    }

}
