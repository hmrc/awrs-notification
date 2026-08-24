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

package models.email

import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import EmailAddressFormats.given
import play.api.libs.json.Reads.*
import play.api.libs.json.Writes.*


case class SendEmailRequest(to: List[EmailAddress], templateId: String, parameters: Map[String, String], force: Boolean, eventUrl: Option[String] = None)

object SendEmailRequest {

  given sendEmailRequestFormat: Format[SendEmailRequest] = (
    (JsPath \ "to").format[List[EmailAddress]] and
      (JsPath \ "templateId").format[String] and
      (JsPath \ "parameters").format[Map[String, String]] and
      (JsPath \ "force").format[Boolean] and
      (JsPath \ "eventUrl").formatNullable[String]
    ) (SendEmailRequest.apply, Tuple.fromProductTyped(_))

}
