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

package config

import models.ApiTypes.ApiType
import models._
import base.BaseSpec

import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class ConfigTest extends BaseSpec with GuiceOneAppPerSuite {

  lazy val UpdateRegistrationTemplate: Some[String] = Some("awrs_notification_template_reg_change")
  lazy val UpdateApplicationTemplate: Some[String] = Some("awrs_notification_template_app_change")
  lazy val API4ApplicationTemplate: Some[String] = Some("awrs_notification_template_comfirmation_api4")
  lazy val API4NewBusApplicationTemplate: Some[String] = Some("awrs_notification_template_comfirmation_api4_new_business")
  lazy val API6ApprovedApplicationTemplate: Some[String] = Some("awrs_notification_template_comfirmation_api6")
  lazy val API6PendingApplicationTemplate: Some[String] = Some("awrs_notification_template_comfirmation_api6_pending")
  lazy val API6NewBusApprovedApplicationTemplate: Some[String] = Some("awrs_notification_template_comfirmation_api6_new_business")
  lazy val API6NewBusPendingApplicationTemplate: Some[String] = Some("awrs_notification_template_comfirmation_api6_new_business_pending")
  lazy val API10CancellationTemplate: Some[String] = Some("awrs_notification_template_cancellation_api10")
  lazy val API8WithdrawnTemplate: Some[String] = Some ("awrs_notification_template_withdrawn_api8")

  def createNotificationRequest(status: Option[String] = None, contactType: Option[ContactTypes.ContactType] = None): PushNotificationRequest =
    PushNotificationRequest("name", "example@example.com", status, contactType, None, variation = false)


  def createConfirmationRequest(apiTypes: ApiType, isNewBusiness: Boolean) =
    EmailRequest(apiTypes, "my business",  "example@example.com", Some("010101"),isNewBusiness = Some(isNewBusiness))

  "Config Test" should {
    "load existing template from config (REJR)" in {
      val result = EmailConfig.getNotificationTemplate(createNotificationRequest(contactType = Some(ContactTypes.REJR)))
      result shouldBe UpdateApplicationTemplate
    }

    "load existing template from config REVR" in {
      val result = EmailConfig.getNotificationTemplate(createNotificationRequest(contactType = Some(ContactTypes.REVR)))
      result shouldBe UpdateRegistrationTemplate
    }

    "load existing template from config CONA" in {
      val result = EmailConfig.getNotificationTemplate(createNotificationRequest(contactType = Some(ContactTypes.CONA)))
      result shouldBe UpdateApplicationTemplate
    }

    "load existing template from config MTRJ" in {
      val result = EmailConfig.getNotificationTemplate(createNotificationRequest(contactType = Some(ContactTypes.MTRJ)))
      result shouldBe UpdateApplicationTemplate
    }

    "load existing template from config NMRJ" in {
      val result = EmailConfig.getNotificationTemplate(createNotificationRequest(contactType = Some(ContactTypes.NMRJ)))
      result shouldBe UpdateApplicationTemplate
    }

    "load existing template from config MTRV" in {
      val result = EmailConfig.getNotificationTemplate(createNotificationRequest(contactType = Some(ContactTypes.MTRV)))
      result shouldBe UpdateRegistrationTemplate
    }

    "load existing template from config NMRV" in {
      val result = EmailConfig.getNotificationTemplate(createNotificationRequest(contactType = Some(ContactTypes.NMRV)))
      result shouldBe UpdateRegistrationTemplate
    }

    "load existing template from config OTHR" in {
      val result = EmailConfig.getNotificationTemplate(createNotificationRequest(contactType = Some(ContactTypes.OTHR)))
      result shouldBe UpdateApplicationTemplate
    }

    "load APPR template from config" in {
      val result = EmailConfig.getNotificationTemplate(createNotificationRequest(status = Some(EmailConfig.Approved)))
      result shouldBe UpdateApplicationTemplate
    }

    "load established business API4 confirmation template from config" in {
      val result = EmailConfig.getConfirmationTemplate(createConfirmationRequest(ApiTypes.API4, isNewBusiness = false))
      result shouldBe API4ApplicationTemplate
    }

    "load new business API4 confirmation template from config" in {
      val result = EmailConfig.getConfirmationTemplate(createConfirmationRequest(ApiTypes.API4, isNewBusiness = true))
      result shouldBe API4NewBusApplicationTemplate
    }

    "load cancellation API10 template from config" in {
      val result = EmailConfig.getCancellationTemplate(EmailRequest(ApiTypes.API10, "my business", "example@example.com"))
      result shouldBe API10CancellationTemplate
    }

    "load withdraw API8 template from config" in {
      val result = EmailConfig.getWithdrawnTemplate(EmailRequest(ApiTypes.API8, "my business", "example@example.com"))
      result shouldBe API8WithdrawnTemplate
    }

    "load established business API6 with pending confirmation template from config" in {
      val result = EmailConfig.getConfirmationTemplate(createConfirmationRequest(ApiTypes.API6Pending, isNewBusiness = false))
      result shouldBe API6PendingApplicationTemplate
    }

    "load established business API6 with approved confirmation template from config" in {
      val result = EmailConfig.getConfirmationTemplate(createConfirmationRequest(ApiTypes.API6Approved, isNewBusiness = false))
      result shouldBe API6ApprovedApplicationTemplate
    }

    "load new business API6 with pending status confirmation template from config" in {
      val result = EmailConfig.getConfirmationTemplate(createConfirmationRequest(ApiTypes.API6Pending, isNewBusiness = true))
      result shouldBe API6NewBusPendingApplicationTemplate
    }

    "load new business API6 with approved status confirmation template from config" in {
      val result = EmailConfig.getConfirmationTemplate(createConfirmationRequest(ApiTypes.API6Approved, isNewBusiness = true))
      result shouldBe API6NewBusApprovedApplicationTemplate
    }
  }
}
