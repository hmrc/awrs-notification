/*
 * Copyright 2025 HM Revenue & Customs
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

package models.email

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}
import PlayJsonFormats._

class PlayJsonFormatsSpec extends AnyWordSpec with Matchers {

  "Reading an EmailAddress from JSON" should {
    "work for a valid email address" in {
      val result = JsString("mailbox@domain.com").validate[EmailAddress]
      result shouldBe a [JsSuccess[_]]
      result.get should be (EmailAddress("mailbox@domain.com"))
    }

    "fail for an invalid email address" in {
      val noDomain = JsString("invalid-email").validate[EmailAddress]
      noDomain shouldBe a [JsError]

      val noMailbox = JsString("@domain.com").validate[EmailAddress]
      noMailbox shouldBe a [JsError]

      val noAt = JsString("mailboxdomain.com").validate[EmailAddress]
      noAt shouldBe a [JsError]

      val empty = JsString("").validate[EmailAddress]
      empty shouldBe a [JsError]
    }
  }

  "Writing an EmailAddress to JSON" should {

    "work" in {
      Json.toJson(EmailAddress("mailbox@domain.com")) should be (JsString("mailbox@domain.com"))
    }
  }
}
