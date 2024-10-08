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

package controllers

import base.BaseSpec
import connectors.EmailConnector
import models.ContactTypes
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.Assertion
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.{StatusNotification, ViewedStatus}
import services.NotificationCacheService
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.{ExecutionContext, Future}


class NotificationCacheControllerTest extends BaseSpec with MockitoSugar with ScalaFutures with GuiceOneAppPerSuite {

  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(Map(
      "metrics.enabled" -> false
    )).build()

  val cc: ControllerComponents = app.injector.instanceOf[ControllerComponents]
  val mockEmailConnector: EmailConnector = mock[EmailConnector]
  val mockNotificationCacheService: NotificationCacheService = mock[NotificationCacheService]
  val mockAuditConnector: AuditConnector = mock[AuditConnector]
  val notificationCacheController = new NotificationCacheController(mockAuditConnector, mockNotificationCacheService, cc, "awrs-notification")

  trait NotificationCacheControllerFixture {
    val notificationCacheController = new NotificationCacheController(mockAuditConnector, mockNotificationCacheService, cc, "awrs-notification")
  }

  "NotificationCacheController" should {

    "return 200 status when the notification is returned successfully" in {
      when(mockNotificationCacheService.findNotification(any())).thenReturn(
        Future.successful(
          Some(StatusNotification(Some("XXAW00000123488"), Some("123456789333"),
          Some(ContactTypes.MTRJ), Some("04"), Some("2017-04-01T0013:07:11"))))
      )

      val result = notificationCacheController.getNotification("XXAW00000123488").apply(FakeRequest().withJsonBody(Json.obj()))
      status(result) shouldBe OK
    }

    "return 404 status when the notification is not cached for valid contact types" in {
      when(mockNotificationCacheService.findNotification(any())).thenReturn(Future.successful(None))

      val result = notificationCacheController.getNotification("XXAW00000123488").apply(FakeRequest().withJsonBody(Json.obj()))
      status(result) shouldBe NOT_FOUND
    }

    "return 200 when the notification delete is called successfully " in {
      when(mockNotificationCacheService.deleteNotification(any())).thenReturn(Future.successful(true))
      val result = notificationCacheController.deleteNotification("XXAW00000123488").apply(FakeRequest())
      status(result) shouldBe OK
    }

    "return 500 when the notification delete fails unexpectedly " in {
      when(mockNotificationCacheService.deleteNotification(any())).thenReturn(Future.successful(false))
      val result = notificationCacheController.deleteNotification("XXAW00000123488").apply(FakeRequest())
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return 200 status and the stored status when the notification viewed status is returned successfully from mongo" in {
      def test(storedViewed: Boolean): Assertion = {
        when(mockNotificationCacheService.findNotificationViewedStatus(any())).thenReturn(
          Future.successful(Some(ViewedStatus(Some("XXAW00000123488"), Some(storedViewed))))
        )

        val result = notificationCacheController.getNotificationViewedStatus("XXAW00000123488").apply(FakeRequest().withJsonBody(Json.obj()))
        status(result) shouldBe OK
        Json.parse(contentAsString(result)).as[ViewedStatus].viewed.get shouldBe storedViewed
      }
      Seq(true, false).foreach(viewed => test(viewed))
    }

    // false is returned because this can only occur on a first visited by a user before a notification was committed to the cache
    "return 200 status and false for viewed when the notification viewed status is not stored in mongo" in {
      when(mockNotificationCacheService.findNotificationViewedStatus(any())).thenReturn(Future.successful(None))

      val result = notificationCacheController.getNotificationViewedStatus("XXAW00000123488").apply(FakeRequest().withJsonBody(Json.obj()))
      Json.parse(contentAsString(result)).as[ViewedStatus].viewed.get shouldBe false
    }

    "return 200 when the mark as viewed call is called successfully " in {
      when(mockNotificationCacheService.markAsViewed(any())).thenReturn(Future.successful(true))
      val result = notificationCacheController.markAsViewed("XXAW00000123488").apply(FakeRequest())
      status(result) shouldBe OK
    }

    "return 500 when the mark as viewed call fails unexpectedly " in {
      when(mockNotificationCacheService.markAsViewed(any())).thenReturn(Future.successful(false))
      val result = notificationCacheController.markAsViewed("XXAW00000123488").apply(FakeRequest())
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

  }

}
