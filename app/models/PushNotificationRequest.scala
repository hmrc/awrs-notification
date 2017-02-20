/*
 * Copyright 2017 HM Revenue & Customs
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

import play.api.i18n.Messages
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import models.AwrsValidator._

import scala.util.{Failure, Success, Try}

case class PushNotificationRequest(name: String, email: String, status: Option[String], contact_type: Option[ContactTypes.ContactType], contact_number: Option[String], variation: Boolean)

object ContactTypes extends Enumeration {

  implicit def convertToString(value: Value): String = value.toString

  type ContactType = Value

  val REJR = Value("REJR")
  val REVR = Value("REVR")
  val CONA = Value("CONA")
  val MTRJ = Value("MTRJ")
  val NMRJ = Value("NMRJ")
  val MTRV = Value("MTRV")
  val NMRV = Value("NMRV")
  val OTHR = Value("OTHR")

  implicit val reader = new Reads[ContactTypes.Value] {

    def reads(js: JsValue): JsResult[ContactTypes.Value] = js match {
      case JsString(s) =>
        Try(ContactTypes.withName(s)) match {
          case Success(value) => JsSuccess(value)
          case Failure(e) => JsError(Messages("contact_type.invalid"))
        }
      case _ => JsError(Messages("error.expected.jsstring"))
    }

  }

  implicit val writer = new Writes[ContactTypes.Value] {
    def writes(contactType: ContactTypes.Value): JsValue = Json.toJson(contactType.toString)
  }

}

object PushNotificationRequest {

  implicit val writer = new Writes[PushNotificationRequest] {

    def writes(push: PushNotificationRequest): JsValue =
      Json.obj(
        "name" -> push.name,
        "email" -> push.email,
        "variation" -> push.variation)
        .++(push.status.fold(Json.obj())(x => Json.obj("status" -> x)))
        .++(push.contact_type.fold(Json.obj())(x => Json.obj("contact_type" -> x)))
        .++(push.contact_number.fold(Json.obj())(x => Json.obj("contact_number" -> x)))

  }

  implicit val pushNotificationRequestFormat: Reads[PushNotificationRequest] = (
    (JsPath \ "name").read[String](verifyingWithError[String](validText(validateISO88591), Messages("name.invalid"))) and
      (JsPath \ "email").read[String](maxLength[String](100) keepAnd pattern(emailRegex, Messages("email.invalid"))) and
      (JsPath \ "status").readNullable[String](pattern(statusRegex, Messages("status.invalid"))) and
      (JsPath \ "contact_type").readNullable[ContactTypes.ContactType] and
      (JsPath \ "contact_number").readNullable[String](pattern(contactNoRegex, Messages("contact_number.invalid"))) and
      (JsPath \ "variation").read[Boolean]
    ) (PushNotificationRequest.apply _)

}
