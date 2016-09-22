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

package mongo

import models.{ContactTypes, PushNotificationRequest}
import org.scalatest.BeforeAndAfterEach
import server.NotificationIntegrationISpec
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.play.http.hooks.HttpHook
import uk.gov.hmrc.play.http.ws.{WSGet, WSPost}

import scala.concurrent.ExecutionContext.Implicits.global

class NotificationControllerISpec extends NotificationIntegrationISpec("NotificationServiceISpec") with UnitSpec with BeforeAndAfterEach with WSPost with WSGet {

  override val hooks: Seq[HttpHook] = NoneRequired

  override protected def beforeEach {
    super.beforeAll()
    await(db().drop())
  }

  implicit val hc = HeaderCarrier()
  val emailUrl = "/awrs-notification/XXAW00000123488"
  val notificationUrl = "/awrs-notification/cache/XXAW00000123488"
  val contentHeaders = Seq(("Content-Type", "application/json"))

  def notification(contactType: ContactTypes.ContactType) = PushNotificationRequest(
    name = "John Doh",
    email = "john.doe@example.com",
    status = Some("04"),
    contact_type = Some(contactType),
    contact_number = Some("123456789012"),
    variation = false
  )

  "Notification cache service" should {

    "return a notification after a successful email call stored a 'Minded to X' notification" in {

      val result = await(doPost(resource(emailUrl), notification(ContactTypes.MTRJ), contentHeaders))
      result.status shouldBe 200

      val notificationResult = await(doGet(resource(notificationUrl)))
      notificationResult.status shouldBe 200
      notificationResult.json.\("registrationNumber").toString should include("XXAW00000123488")
      notificationResult.json.\("contactNumber").toString should include("123456789012")
      notificationResult.json.\("contactType").toString should include("MTRJ")
      notificationResult.json.\("status").toString should include("04")
    }

    "return a NOT FOUND after a successful email call stored a 'No Longer X' notification" in {

      val result = await(doPost(resource(emailUrl), notification(ContactTypes.NMRJ), contentHeaders))
      result.status shouldBe 200

      val notificationResult = await(doGet(resource(notificationUrl)))
      notificationResult.status shouldBe 404
    }

    "return a NOT FOUND after a successful email call for a normal, i.e. non 'Minded to' notification" in {

      val result = await(doPost(resource(emailUrl), notification(ContactTypes.REJR), contentHeaders))
      result.status shouldBe 200

      val notificationResult = await(doGet(resource(notificationUrl)))
      notificationResult.status shouldBe 404
    }
  }
}
