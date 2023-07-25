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

package utils

object EmailHelper {
  private val notificationTemplateMap: Map[String, String] = Map(
    "awrs.notification.REJR" -> "awrs_notification_template_app_change",
    "awrs.notification.REVR" -> "awrs_notification_template_reg_change",
    "awrs.notification.CONA" -> "awrs_notification_template_app_change",
    "awrs.notification.MTRJ" -> "awrs_notification_template_app_change",
    "awrs.notification.NMRJ" -> "awrs_notification_template_app_change",
    "awrs.notification.MTRV" -> "awrs_notification_template_reg_change",
    "awrs.notification.NMRV" -> "awrs_notification_template_reg_change",
    "awrs.notification.OTHR" -> "awrs_notification_template_app_change",
    "awrs.notification.APPR" -> "awrs_notification_template_app_change",
    "awrs.notification.DFLT" -> "awrs_notification_template_app_change"
  )

  def notificationTemplate(key: String): Option[String] =
    notificationTemplateMap.view.filterKeys(_ == key).values.headOption

  private val otherTemplatesMap: Map[String, String] = Map(
    "awrs.confirmation.api4" -> "awrs_notification_template_comfirmation_api4",
    "awrs.confirmation.api6.approved" -> "awrs_notification_template_comfirmation_api6",
    "awrs.confirmation.api6.pending" -> "awrs_notification_template_comfirmation_api6_pending",
    "awrs.confirmation.new_business.api4" -> "awrs_notification_template_comfirmation_api4_new_business",
    "awrs.confirmation.new_business.api6.approved" -> "awrs_notification_template_comfirmation_api6_new_business",
    "awrs.confirmation.new_business.api6.pending" -> "awrs_notification_template_comfirmation_api6_new_business_pending",
    "awrs.cancellation.api10" -> "awrs_notification_template_cancellation_api10",
    "awrs.withdrawn.api8" -> "awrs_notification_template_withdrawn_api8"
  )

  def otherTemplates(key: String): Option[String] = {
    otherTemplatesMap.view.filterKeys(_ == key).values.headOption
  }

  private val callBackEventsMap: Map[String, String] = Map(
    "delivered" -> "Delivered",
    "permanentbounce" -> "PermanentBounce",
    "sent" -> "Sent",
    "opened" -> "Opened"
  )

  def callBackEvents(key: String): Option[String] =
    callBackEventsMap.view.filterKeys(_ == key).values.headOption
}
