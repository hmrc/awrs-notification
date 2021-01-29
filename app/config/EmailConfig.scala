/*
 * Copyright 2021 HM Revenue & Customs
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

import models.{EmailRequest, PushNotificationRequest}
import utils.EmailHelper._

trait EmailConfig {
  lazy val ApprovedTemplate = "APPR"
  lazy val DefaultTemplate = "DFLT"
  lazy val Approved = "04"

  def getNotificationTemplate(notificationRequest: PushNotificationRequest): Option[String] = {
    val template = (notificationRequest.contact_type, notificationRequest.status) match {
      case (Some(contactType), _) => contactType
      case (_, Some(Approved)) => ApprovedTemplate
      case (_, _) => DefaultTemplate
    }

    notificationTemplate(s"awrs.notification.$template")

  }

  def getConfirmationTemplate(emailRequest: EmailRequest): Option[String] = {
    val templateName = s"awrs.confirmation.${
      emailRequest.isNewBusiness match {
        case Some(true) => "new_business."
        case Some(false) => ""
        case _ => ""
      }
    }${emailRequest.apiType.toString}"
    otherTemplates(templateName)
  }

  def getCancellationTemplate(emailRequest: EmailRequest): Option[String] = {
    val templateName = s"awrs.cancellation.${emailRequest.apiType.toString}"
    otherTemplates(templateName)
  }

  def getWithdrawnTemplate(emailRequest: EmailRequest): Option[String] = {
    val templateName = s"awrs.withdrawn.${emailRequest.apiType.toString}"
    otherTemplates(templateName)
  }

}

object EmailConfig extends EmailConfig
