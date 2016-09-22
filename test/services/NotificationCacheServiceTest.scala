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

import models.{ContactTypes, PushNotificationRequest}
import org.mockito.Matchers._
import org.mockito.Mock
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite
import reactivemongo.api.commands.WriteResult
import repositories.{NotificationRepository, StatusNotification}
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class NotificationCacheServiceTest extends UnitSpec with MockitoSugar with OneServerPerSuite {

  val mockNotificationRepository = mock[NotificationRepository]
  val mockHeaderCarrier = mock[HeaderCarrier]

  val notificationCacheService = new NotificationCacheService {
    override val repository = mockNotificationRepository
  }

  "NotificationCacheService" should {
    "return StatusNotification object when the notification is found in mongo" in {

      val notification = Some(StatusNotification(Some("XXAW00000123488"), Some("123456789333"), Some(ContactTypes.MTRJ), Some("04")))

      when(mockNotificationRepository.findByRegistrationNumber(any())).thenReturn(notification)

      val result = Await.result(notificationCacheService.findNotification("XXAW00000123488")(hc = mockHeaderCarrier), 2.second)

      result.get.contactType shouldBe notification.get.contactType
      result.get.status shouldBe notification.get.status
      result.get.registrationNumber shouldBe notification.get.registrationNumber
      result.get.contactNumber shouldBe notification.get.contactNumber
    }

    "return None when the notification is found in mongo but not the correct contact type" in {

      val notification = Some(StatusNotification(Some("XXAW00000123488"), Some("123456789333"), Some(ContactTypes.OTHR), Some("04")))

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

    "return false when the notification is not stored in mongo because it is not the correct contact type" in {

      val push = PushNotificationRequest(name = "name", email = "exampe@example.com", status = Some("04"), contact_type = Some(ContactTypes.REJR), contact_number = Some("123456789012"), variation = false)

      val result = Await.result(notificationCacheService.storeNotification(push, "XXAW00000123488")(hc = mockHeaderCarrier), 2.second)

      result shouldBe false
    }

    "return true when the notification is stored in mongo" in {

      val push = PushNotificationRequest(name = "name", email = "exampe@example.com", status = Some("04"), contact_type = Some(ContactTypes.NMRV), contact_number = Some("123456789012"), variation = false)

      when(mockNotificationRepository.insertStatusNotification(any())).thenReturn(Future.successful(true))

      val result = Await.result(notificationCacheService.storeNotification(push, "XXAW00000123488")(hc = mockHeaderCarrier), 2.second)

      result shouldBe true
    }

    "return true when the notification is deleted from mongo" in {

      val writeResult = mock[WriteResult]

      when(mockNotificationRepository.deleteStatusNotification(any())).thenReturn(writeResult)
      when(writeResult.ok).thenReturn(true)

      val result = Await.result(notificationCacheService.deleteNotification("XXAW00000123488")(hc = mockHeaderCarrier), 2.second)

      result shouldBe((true, None))
    }

    "return false when an unexpected error occurs" in {

      val writeResult = mock[WriteResult]
      val error = Some("Unexpected Error")

      when(mockNotificationRepository.deleteStatusNotification(any())).thenReturn(writeResult)
      when(writeResult.ok).thenReturn(false)
      when(writeResult.errmsg).thenReturn(error)

      val result = Await.result(notificationCacheService.deleteNotification("XXAW00000123488")(hc = mockHeaderCarrier), 2.second)

      result shouldBe((false, error))
    }

  }

}
