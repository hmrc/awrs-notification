/*
 * Copyright 2019 HM Revenue & Customs
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

import models.{ContactTypes, PushNotificationRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.commands.WriteResult.Message
import repositories.{NotificationRepository, NotificationViewedRepository, StatusNotification, ViewedStatus}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class NotificationCacheServiceTest extends UnitSpec with MockitoSugar with GuiceOneAppPerSuite {

  val mockNotificationRepository: NotificationRepository = mock[NotificationRepository]
  val mockAuditConnector: AuditConnector = mock[AuditConnector]
  val mockNotificationViewedRepository: NotificationViewedRepository = mock[NotificationViewedRepository]
  val mockHeaderCarrier: HeaderCarrier = mock[HeaderCarrier]
  override implicit lazy val app: Application = new GuiceApplicationBuilder().configure(Map("metrics.enabled" -> false)).build()

  val notificationCacheService: NotificationCacheService = new NotificationCacheService(mockAuditConnector, mockNotificationRepository,
    mockNotificationViewedRepository,"awrs")


  "NotificationCacheService" should {
    "return StatusNotification object when the notification is found in mongo" in {
      val notification = Some(StatusNotification(Some("XXAW00000123488"), Some("123456789333"), Some(ContactTypes.MTRJ), Some("04"), Some("2017-04-01T0013:07:11")))
      when(mockNotificationRepository.findByRegistrationNumber(any())).thenReturn(notification)
      val result = Await.result(notificationCacheService.findNotification("XXAW00000123488")(hc = mockHeaderCarrier), 2.second)
      result.get.contactType shouldBe notification.get.contactType
      result.get.status shouldBe notification.get.status
      result.get.registrationNumber shouldBe notification.get.registrationNumber
      result.get.contactNumber shouldBe notification.get.contactNumber
      result.get.storageDatetime shouldBe notification.get.storageDatetime
    }

    "return None when the notification is found in mongo but not the correct contact type" in {
      val notification = Some(StatusNotification(Some("XXAW00000123488"), Some("123456789333"), Some(ContactTypes.OTHR), Some("04"), Some("2017-04-01T0013:07:11")))
      when(mockNotificationRepository.findByRegistrationNumber(any())).thenReturn(notification)
      val result = Await.result(notificationCacheService.findNotification("XXAW00000123488")(hc = mockHeaderCarrier), 2.second)
      result.isDefined shouldBe false
    }

    "return None when the notification is not found in mongo" in {
      val notification = None
      when(mockNotificationRepository.findByRegistrationNumber(any())).thenReturn(notification)
      val result = Await.result(notificationCacheService.findNotification("XXAW00000123488")(hc = mockHeaderCarrier), 2.second)
      result.isDefined shouldBe false
    }

    "return false when the notification is not stored in mongo because it is not the correct contact type (CONA)" in {
      val push = PushNotificationRequest(name = "name", email = "exampe@example.com", status = Some("04"),
        contact_type = Some(ContactTypes.CONA), contact_number = Some("123456789012"), variation = false)
      val result = Await.result(notificationCacheService.storeNotification(push, "XXAW00000123488")(hc = mockHeaderCarrier), 2.second)
      result shouldBe false
    }

    "return false when the notification is not stored in mongo because it is not the correct contact type (OTHR)" in {
      val push = PushNotificationRequest(name = "name", email = "exampe@example.com", status = Some("04"), contact_type = Some(ContactTypes.OTHR),
        contact_number = Some("123456789012"), variation = false)
      val result = Await.result(notificationCacheService.storeNotification(push, "XXAW00000123488")(hc = mockHeaderCarrier), 2.second)
      result shouldBe false
    }

    "return false when the notification is not stored in mongo because it is not the correct contact type (NMRJ)" in {
      val push = PushNotificationRequest(name = "name", email = "exampe@example.com", status = Some("04"), contact_type = Some(ContactTypes.NMRJ),
        contact_number = Some("123456789012"), variation = false)
      val result = Await.result(notificationCacheService.storeNotification(push, "XXAW00000123488")(hc = mockHeaderCarrier), 2.second)
      result shouldBe false
    }


    "return true when the notification is stored in mongo (NMRJ)" in {
      val push = PushNotificationRequest(name = "name", email = "exampe@example.com", status = Some("04"), contact_type = Some(ContactTypes.REJR),
        contact_number = Some("123456789012"), variation = false)
      when(mockNotificationRepository.insertStatusNotification(any())).thenReturn(Future.successful(true))
      val result: Boolean = Await.result(notificationCacheService.storeNotification(push, "XXAW00000123488")(hc = mockHeaderCarrier), 2.second)
      result shouldBe true
    }

    "return true when the notification is stored in mongo (REVR)" in {
      val push = PushNotificationRequest(name = "name", email = "exampe@example.com", status = Some("04"), contact_type = Some(ContactTypes.REVR), contact_number = Some("123456789012"), variation = false)
      when(mockNotificationRepository.insertStatusNotification(any())).thenReturn(Future.successful(true))
      val result = Await.result(notificationCacheService.storeNotification(push, "XXAW00000123488")(hc = mockHeaderCarrier), 2.second)
      result shouldBe true
    }

    "return true when the notification is stored in mongo (MTRJ)" in {
      val push = PushNotificationRequest(name = "name", email = "exampe@example.com", status = Some("04"), contact_type = Some(ContactTypes.MTRJ), contact_number = Some("123456789012"), variation = false)
      when(mockNotificationRepository.insertStatusNotification(any())).thenReturn(Future.successful(true))
      val result = Await.result(notificationCacheService.storeNotification(push, "XXAW00000123488")(hc = mockHeaderCarrier), 2.second)
      result shouldBe true
    }

    "return true when the notification is stored in mongo (MTRV)" in {
      val push = PushNotificationRequest(name = "name", email = "exampe@example.com", status = Some("04"), contact_type = Some(ContactTypes.MTRV), contact_number = Some("123456789012"), variation = false)
      when(mockNotificationRepository.insertStatusNotification(any())).thenReturn(Future.successful(true))
      val result = Await.result(notificationCacheService.storeNotification(push, "XXAW00000123488")(hc = mockHeaderCarrier), 2.second)
      result shouldBe true
    }

    "return true when the notification is stored in mongo (NMRV)" in {
      val push = PushNotificationRequest(name = "name", email = "exampe@example.com", status = Some("04"), contact_type = Some(ContactTypes.NMRV),
        contact_number = Some("123456789012"), variation = false)
      when(mockNotificationRepository.insertStatusNotification(any())).thenReturn(Future.successful(true))
      val result = Await.result(notificationCacheService.storeNotification(push, "XXAW00000123488")(hc = mockHeaderCarrier), 2.second)
      result shouldBe true
    }

    "return true when the notification is deleted from mongo" in {
      val writeResult = mock[WriteResult]
      when(mockNotificationRepository.deleteStatusNotification(any())).thenReturn(writeResult)
      when(writeResult.ok).thenReturn(true)
      val result = Await.result(notificationCacheService.deleteNotification("XXAW00000123488")(hc = mockHeaderCarrier), 2.second)
      result shouldBe ((true, None))
    }

    "return false when an unexpected error occurs" in {
      val writeResult = mock[WriteResult]
      val error = Some("Unexpected Error")
      when(mockNotificationRepository.deleteStatusNotification(any())).thenReturn(writeResult)
      when(writeResult.ok).thenReturn(false)
      when(Message.unapply(writeResult)).thenReturn(error)
      val result = Await.result(notificationCacheService.deleteNotification("XXAW00000123488")(hc = mockHeaderCarrier), 2.second)
      result shouldBe ((false, error))
    }

    "return the viewed status found in mongo when it exists" in {
      val viewedStatus = Some(ViewedStatus(Some("XXAW00000123488"), Some(false)))
      when(mockNotificationViewedRepository.findViewedStatusByRegistrationNumber(any())).thenReturn(viewedStatus)
      val result = Await.result(notificationCacheService.findNotificationViewedStatus("XXAW00000123488")(hc = mockHeaderCarrier), 2.second)
      result shouldBe viewedStatus
    }

    "return None when the viewed status is not found in mongo" in {
      when(mockNotificationViewedRepository.findViewedStatusByRegistrationNumber(any())).thenReturn(None)
      val result = Await.result(notificationCacheService.findNotificationViewedStatus("XXAW00000123488")(hc = mockHeaderCarrier), 2.second)
      result.isDefined shouldBe false
    }

    "return true when the viewed status is stored in mongo" in {
      when(mockNotificationViewedRepository.insertViewedStatus(any())).thenReturn(Future.successful(result = true))
      val result = Await.result(notificationCacheService.storeNotificationViewedStatus(viewedStatus = false,
        "XXAW00000123488")(hc = mockHeaderCarrier), 2.second)
      result shouldBe true
    }

    "return true when the viewed status is updated in mongo" in {
      val writeResult = mock[WriteResult]
      when(mockNotificationViewedRepository.markAsViewed(any())).thenReturn(writeResult)
      when(writeResult.ok).thenReturn(true)
      val result = Await.result(notificationCacheService.markAsViewed("XXAW00000123488")(hc = mockHeaderCarrier), 2.second)
      result shouldBe ((true, None))
    }

    "return false when an unexpected error occurs when calling the mark as viewed service" in {
      val writeResult = mock[WriteResult]
      val error = Some("Unexpected Error")
      when(mockNotificationViewedRepository.markAsViewed(any())).thenReturn(writeResult)
      when(writeResult.ok).thenReturn(false)
      when(Message.unapply(writeResult)).thenReturn(error)
      val result = Await.result(notificationCacheService.markAsViewed("XXAW00000123488")(hc = mockHeaderCarrier), 2.second)
      result shouldBe ((false, error))
    }
  }
}
