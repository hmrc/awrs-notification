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

package utils

object ErrorNotifications {

  val invalidName: String = "Invalid name"
  val invalidStatus = "Invalid status"
  val invalidContactNumber = "Invalid contact number"
  val invalidRegNumber = "Invalid registration number"
  val invalidContactType = "Invalid contact type"
  val invalidTemplate = "Template does not exist for provided contact type"
  val invalidEmail = "Invalid email address"
  val invalidUnknown = "Unknown reason. Dependent system did not provide failure reason"
  val invalidWErrorBuilder = "Error builder failed to extract error message"
  val invalidContentType = "Invalid request content type"
  val errorMaxLength = "Invalid email address"
  val errorExpectedBoolean = "Boolean value expected"
  val errorExpectedString = "String value expected"
  val invalidUri = "URI not found"
  val invalidApiType = "Invalid API type"

  private val validationErrorMap: Map[String, String] = Map(

    "name.invalid" -> invalidName,
    "status.invalid" -> invalidStatus,
    "contact_number.invalid" -> invalidContactNumber,
    "registration_number.invalid" -> invalidRegNumber,
    "contact_type.invalid" -> invalidContactType,
    "template_mapping.error" -> invalidTemplate,
    "email.invalid" -> invalidEmail,
    "unknown.failure" -> invalidUnknown,
    "error_builder.failure" -> invalidWErrorBuilder,
    "content_type.invalid" -> invalidContentType,
    "error.maxLength" -> errorMaxLength,
    "error.expected.jsboolean" -> errorExpectedBoolean,
    "error.expected.jsstring" -> errorExpectedString,
    "uri.invalid" -> invalidUri,
    "api_type.invalid" -> invalidApiType
  )

  def getError(key: String): String =
    validationErrorMap.filterKeys(_ == key).values.headOption.fold(key)(x => x)
}
