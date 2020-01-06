/*
 * Copyright 2020 HM Revenue & Customs
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

import models.EmailResponse
import play.api.libs.json.{JsPath, JsonValidationError}
import uk.gov.hmrc.http.HttpResponse
import play.api.http.Status._
import ErrorNotifications._

object ErrorHandling {

   def extractResponseMessage(response: HttpResponse): String =
    response.header("Content-Type") match {
      case Some(headerType) if headerType.contains("application/json") =>
        if (response.json.toString().contains("message"))
          (response.json \ "message").toString.replaceAll("\"", "")
        else
          response.json.toString()
      case _ =>
        response.body
    }

   def getValidationError(err: Seq[(JsPath, Seq[JsonValidationError])]): EmailResponse =
    err match {
      case head :: tail =>
        head._2.headOption.map(_.message) match {
          case Some(message) =>
            EmailResponse(BAD_REQUEST, Some(getError(message)))
          case _ =>
            EmailResponse(INTERNAL_SERVER_ERROR, Some(invalidWErrorBuilder))
        }
      case _ =>
        EmailResponse(INTERNAL_SERVER_ERROR, Some(invalidWErrorBuilder))
    }

}
