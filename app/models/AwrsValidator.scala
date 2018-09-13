/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.data.validation.ValidationError
import play.api.libs.json.Reads
import play.api.libs.json.Reads._

object AwrsValidator extends AwrsValidator

trait AwrsValidator {

  val registrationRegex = "^X[A-Z]AW00000[0-9]{6}$"

  val statusRegex = "0[4-9]|10".r

  val contactNoRegex = "[0-9]{12}".r

  val emailRegex = """(^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$)""".r

  val asciiChar32 = 32
  val asciiChar126 = 126
  val asciiChar160 = 160
  val asciiChar255 = 255
  val maxTextLength = 140

  def verifyingWithError[A](cond: A => Boolean, error: String = "error.invalid")(implicit rds: Reads[A]) =
    filter[A](ValidationError(error))(cond)(rds)

  def validText(validationFunction: String => Boolean)(inputText: String) = exceedsMaxLength(inputText) match {
    case true => false
    case _ => validationFunction(inputText)
  }

  private def exceedsMaxLength(text: String) = text.length > maxTextLength

  def validateISO88591(input: String): Boolean = {
    val inputList: List[Char] = input.toList
    inputList.forall { c =>
      (c >= asciiChar32 && c <= asciiChar126) || (c >= asciiChar160 && c <= asciiChar255)
    }
  }
  
}
