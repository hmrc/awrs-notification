/*
 * Copyright 2022 HM Revenue & Customs
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

import models.AwrsValidator._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import utils.ErrorNotifications._

import scala.util.{Failure, Success, Try}

case class PushNotificationRequest(name: String, email: String, status: Option[String],
                                    contact_type: Option[ContactTypes.ContactType], contact_number: Option[String],
                                    variation: Boolean)

object ContactTypes extends Enumeration {

  implicit def convertToString(value: Value): String = value.toString

  type ContactType = Value

    val REJR: ContactTypes.Value = Value("REJR")//Rejected
    val REVR: ContactTypes.Value = Value("REVR")//Revoked
    val CONA: ContactTypes.Value = Value("CONA")//Approved with Conditions
    val MTRJ: ContactTypes.Value = Value("MTRJ")//Minded to Reject
    val NMRJ: ContactTypes.Value = Value("NMRJ")//No longer minded to Reject
    val MTRV: ContactTypes.Value = Value("MTRV")//Minded to Revoke
    val NMRV: ContactTypes.Value = Value("NMRV")//No longer minded to Revoke
    val OTHR: ContactTypes.Value = Value("OTHR")//Other

  implicit val reader: Reads[ContactTypes.Value] = {
    case JsString(s) =>
      Try(ContactTypes.withName(s)) match {
        case Success(value) => JsSuccess(value)
        case Failure(_) => JsError(invalidContactType)
      }
    case _ => JsError(errorExpectedString)
  }

  implicit val writer: Writes[ContactTypes.Value] =
    (contactType: ContactTypes.Value) => Json.toJson(contactType.toString)

}

object PushNotificationRequest {

  val maxEmailLength: Int = 100

  implicit val writer: Writes[PushNotificationRequest] = (push: PushNotificationRequest) => Json.obj(
    "name" -> push.name,
    "email" -> push.email,
    "variation" -> push.variation)
    .++(push.status.fold(Json.obj())(x => Json.obj("status" -> x)))
    .++(push.contact_type.fold(Json.obj())(x => Json.obj("contact_type" -> x)))
    .++(push.contact_number.fold(Json.obj())(x => Json.obj("contact_number" -> x)))

  implicit val pushNotificationRequestFormat: Reads[PushNotificationRequest] = (
    (JsPath \ "name").read[String](verifyingWithError[String](validText(validateISO88591), invalidName)) and
      (JsPath \ "email").read[String](maxLength[String](maxEmailLength) keepAnd pattern(emailRegex, invalidEmail)) and
      (JsPath \ "status").readNullable[String](pattern(statusRegex, invalidStatus)) and
      (JsPath \ "contact_type").readNullable[ContactTypes.ContactType] and
      (JsPath \ "contact_number").readNullable[String](pattern(contactNoRegex, invalidContactNumber)) and
      (JsPath \ "variation").read[Boolean]
    ) (PushNotificationRequest.apply _)

}
