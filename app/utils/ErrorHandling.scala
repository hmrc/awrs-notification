/*
 * Copyright 2019 HM Revenue & Customs
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

import config.ErrorConfig
import models.EmailResponse
import play.api.data.validation.ValidationError
import play.api.libs.json.JsPath
import uk.gov.hmrc.http.HttpResponse

object ErrorHandling {

   def extractResponseMessage(response: HttpResponse): String =
    response.header("Content-Type") match {
      case Some(headerType) if headerType.contains("application/json") =>
        if (response.json.toString().contains("message"))
          (response.json \ "message" ).toString().replaceAll("\"", "")
        else
          response.json.toString()
      case _ =>
        response.body
    }

   def getValidationError(err: Seq[(JsPath, Seq[ValidationError])]): EmailResponse =
    err match {
      case head :: tail =>
        head._2.headOption.map(_.message) match {
          case Some(message) =>
            EmailResponse(400, Some(ErrorConfig.getError(message)))
          case _ =>
            EmailResponse(500, Some(ErrorConfig.invalidWErrorBuilder))
        }
      case _ =>
        EmailResponse(500, Some(ErrorConfig.invalidWErrorBuilder))
    }

}
