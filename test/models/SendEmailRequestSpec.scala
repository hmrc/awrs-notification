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

package models

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json._
import base.BaseSpec
import models.email.SendEmailRequest


class SendEmailRequestSpec extends BaseSpec with GuiceOneAppPerSuite {

  "SendEmailRequest" should {
    "transform a PushNotification model into JSON" in {
      val sendEmailRequest = SendEmailRequest(List(), "templateId", Map.empty[String, String], force = true, None)

      Json.toJson(sendEmailRequest)
    }

    "transform a valid JSON to PushNotification model object" in {
      val inputJson = Json.parse(
        """{
          |"to":[],
          |"templateId":"templateId",
          |"parameters":{
          | "param":"val"
          |},
          |"force":false
          |}""".stripMargin)
      val sendEmailRequest = SendEmailRequest(List(), "templateId", Map("param" -> "val"), force = false, None)
      val result = inputJson.as[SendEmailRequest]

      result shouldBe sendEmailRequest
    }
  }
}
