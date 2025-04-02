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

import models.email.{Domain, EmailAddress}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class EmailAddressSpec extends AnyWordSpec with ScalaCheckPropertyChecks with Matchers {

  "Creating an EmailAddress class" should {
    "work for a valid email address" in {
      EmailAddress("mailbox@domain.com").value should be("mailbox@domain.com")
    }

    "throw an exception for an invalid email" in {
      an[IllegalArgumentException] should be thrownBy EmailAddress("invalid-email")
    }


    "throw an exception for a valid email starting with invalid characters" in {
      an[IllegalArgumentException] should be thrownBy EmailAddress("§mailbox@domain.com")
    }

    "throw an exception for a valid email ending with an invalid character" in {
      an[IllegalArgumentException] should be thrownBy EmailAddress("mailbox@domain.com!")
    }

    "throw an exception when the '@' is missing" in {
      an[IllegalArgumentException] should be thrownBy EmailAddress("mailboxdomain.com")
    }

    "throw an exception for an empty email" in {
      an[IllegalArgumentException] should be thrownBy EmailAddress("")
    }

    "throw an exception for a repeated email" in {
      an[IllegalArgumentException] should be thrownBy EmailAddress("mailbox@domain.commailbox@domain.com")
    }

    "throw an exception for a missing mailbox" in {
      an[IllegalArgumentException] should be thrownBy EmailAddress("@domain.com")
    }

    "throw an exception for a missing domain" in {
      an[IllegalArgumentException] should be thrownBy EmailAddress("mailbox@")
    }
  }


  "An email address domain" should {
    "be extractable from an address" in {
      EmailAddress("mailbox@domain.com").domain.value should be("domain.com")
    }

    "throw an exception for an invalid domain" in {
      an[IllegalArgumentException] should be thrownBy Domain("domain.")
      an[IllegalArgumentException] should be thrownBy Domain(".com")
      an[IllegalArgumentException] should be thrownBy Domain("domain..com")
      an[IllegalArgumentException] should be thrownBy Domain("")
      an[IllegalArgumentException] should be thrownBy Domain(" ")
    }

  }

  "An email address mailbox" should {
    "be extractable from an address" in {
      EmailAddress("mailbox@domain.com").mailbox.value should be("mailbox")
    }
  }
}


