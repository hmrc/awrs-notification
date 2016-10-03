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

package config

import models.{ConfirmationEmailRequest, PushNotificationRequest}
import play.api.Play._


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
    configuration.getString(s"awrs.notification.$template")
  }

  def getConfirmationTemplate(confirmationEmailRequest: ConfirmationEmailRequest): Option[String] =
    configuration.getString(s"awrs.confirmation.${confirmationEmailRequest.apiType.toString}")

}

object EmailConfig extends EmailConfig
