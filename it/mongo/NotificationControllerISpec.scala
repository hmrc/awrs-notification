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

import akka.actor.ActorSystem
import com.typesafe.config.Config
import models.{ContactTypes, PushNotificationRequest}
import org.scalatest.BeforeAndAfterEach
import play.api.Play
import server.NotificationIntegrationISpec
import uk.gov.hmrc.http.{HeaderCarrier, HttpDelete, HttpGet, HttpPost}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.play.http.ws.{WSDelete, WSGet, WSPost}

import scala.concurrent.ExecutionContext.Implicits.global

class NotificationControllerISpec extends NotificationIntegrationISpec("NotificationServiceISpec") with UnitSpec
  with BeforeAndAfterEach with WSPost with HttpPost with WSGet with HttpGet with WSDelete with HttpDelete{

  override val hooks: Seq[HttpHook] = Seq()

  override protected def beforeEach {
    super.beforeAll()
    await(db().drop())
  }

  implicit val hc = HeaderCarrier()
  val emailUrl = "/awrs-notification/XXAW00000123488"
  val notificationUrl = "/awrs-notification/cache/XXAW00000123488"
  val viewedStatusUrl = "/awrs-notification/cache/viewed/XXAW00000123488"
  val contentHeaders = Seq(("Content-Type", "application/json"))

  def notification(contactType: ContactTypes.ContactType) = PushNotificationRequest(
    name = "name",
    email = "example@example.com",
    status = Some("04"),
    contact_type = Some(contactType),
    contact_number = Some("123456789012"),
    variation = false
  )

  "Notification cache service" should {

    "return a notification and view status after a successful email call stored a 'Minded to X' notification" in {

      val result = await(doPost(resource(emailUrl), notification(ContactTypes.MTRJ), contentHeaders))
      result.status shouldBe 204

      val notificationResult = await(doGet(resource(notificationUrl)))
      notificationResult.status shouldBe 200
      notificationResult.json.\("registrationNumber").toString should include("XXAW00000123488")
      notificationResult.json.\("contactNumber").toString should include("123456789012")
      notificationResult.json.\("contactType").toString should include("MTRJ")
      notificationResult.json.\("status").toString should include("04")

      val viewedStatusResult = await(doGet(resource(viewedStatusUrl)))
      viewedStatusResult.status shouldBe 200
      viewedStatusResult.json.toString should include("false")
    }

    "return a NOT FOUND notification but a view status after a successful email call stored a 'No Longer X' notification" in {

      val result = await(doPost(resource(emailUrl), notification(ContactTypes.NMRJ), contentHeaders))
      result.status shouldBe 204

      val notificationResult = await(doGet(resource(notificationUrl)))
      notificationResult.status shouldBe 404

      val viewedStatusResult = await(doGet(resource(viewedStatusUrl)))
      viewedStatusResult.status shouldBe 200
      viewedStatusResult.json.toString should include("false")
    }

    "return a NOT FOUND notification but a view status after a successful email call for a normal, i.e. non 'Minded to' notification" in {

      val result = await(doPost(resource(emailUrl), notification(ContactTypes.REJR), contentHeaders))
      result.status shouldBe 204

      val notificationResult = await(doGet(resource(notificationUrl)))
      notificationResult.status shouldBe 404

      val viewedStatusResult = await(doGet(resource(viewedStatusUrl)))
      viewedStatusResult.status shouldBe 200
      viewedStatusResult.json.toString should include("false")
    }

    "return a NOT FOUND after a successful email call stored a 'Minded to X' notification but after it has been deleted" in {
      for {
        result <- doPost(resource(emailUrl), notification(ContactTypes.MTRJ), contentHeaders)
        notificationResult <- doGet(resource(notificationUrl))
        _ <- doDelete(resource(notificationUrl))
        notificationResultGetAfterDelete <- doGet(resource(notificationUrl))
      } yield {
        result.status shouldBe 200
        notificationResult.status shouldBe 200
        notificationResultGetAfterDelete.status shouldBe 404
      }
    }

    "return 'true' view status after a successful email call but after the status has been deleted from mongo" in {

      for {
        result <- doPost(resource(emailUrl), notification(ContactTypes.REJR), contentHeaders)
        viewedStatusResultGet <- doGet(resource(viewedStatusUrl))
        _ <- doDelete(resource(viewedStatusUrl))
        viewedStatusResultGetAfterDelete <- doGet(resource(viewedStatusUrl))
      } yield {
        result.status shouldBe 200
        viewedStatusResultGet.status shouldBe 200
        viewedStatusResultGet.json.toString should include("false")
        viewedStatusResultGetAfterDelete.status shouldBe 200
        viewedStatusResultGetAfterDelete.json.toString should include("true")
      }
    }
  }

  override protected def actorSystem: ActorSystem = Play.current.actorSystem

  override protected def configuration: Option[Config] = Some(Play.current.configuration.underlying)
}
