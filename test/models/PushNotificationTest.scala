/*
 * Copyright 2020 HM Revenue & Customs
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

package models

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json._
import uk.gov.hmrc.play.test.UnitSpec

class PushNotificationTest extends UnitSpec with GuiceOneAppPerSuite {

  "Push Notification" should {
    "transform a PushNotification model into JSON" in {
      val pushNotification: PushNotificationRequest = PushNotificationRequest("name", "some@some.com", Some("04"), Some(ContactTypes.REJR), Some("123456789012"), false)

      Json.toJson(pushNotification)
    }

    "transform a PushNotification model into JSON with missing data" in {
      val pushNotification: PushNotificationRequest = PushNotificationRequest("name", "some@some.com", None, None, None, variation = false)

      Json.toJson(pushNotification)
    }

    "transform a valid JSON to PushNotification model object" in {
      val inputJson = Json.parse( """{"name":"name","email":"some@some.com","status":"04","contact_type":"REJR","contact_number":"123456789012","variation":false}""")
      val pushNotification = PushNotificationRequest("name", "some@some.com", Some("04"), Some(ContactTypes.REJR), Some("123456789012"), false)

      val result = inputJson.as[PushNotificationRequest]

      result shouldBe pushNotification
    }

    "transform a valid JSON to PushNotification model object without contact number" in {
      val inputJson = Json.parse( """{"name":"name","email":"some@some.com","status":"04","contact_type":"REJR","variation":false}""")
      val pushNotification = PushNotificationRequest("name", "some@some.com", Some("04"), Some(ContactTypes.REJR), None, false)

      val result = inputJson.as[PushNotificationRequest]

      result shouldBe pushNotification
    }

    "transform a valid JSON to PushNotification model object without status" in {
      val inputJson = Json.parse( """{"name":"name","email":"some@some.com","contact_type":"REJR","contact_number":"123456789012","variation":false}""")
      val pushNotification = PushNotificationRequest("name", "some@some.com", None, Some(ContactTypes.REJR), Some("123456789012"), false)

      val result = inputJson.as[PushNotificationRequest]

      result shouldBe pushNotification
    }

    "transform a valid JSON to PushNotification model object without contact type" in {
      val inputJson = Json.parse( """{"name":"name","email":"some@some.com","status":"04","contact_number":"123456789012","variation":false}""")
      val pushNotification = PushNotificationRequest("name", "some@some.com", Some("04"), None, Some("123456789012"), false)

      val result = inputJson.as[PushNotificationRequest]

      result shouldBe pushNotification
    }
  }
}
