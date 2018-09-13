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

package config

import play.api.Play._

trait ErrorConfig {
  lazy val invalidName = configuration.getString("name.invalid").fold("")(x => x)
  lazy val invalidStatus = configuration.getString("status.invalid").fold("")(x => x)
  lazy val invalidContactNumber = configuration.getString("contact_number.invalid").fold("")(x => x)
  lazy val invalidRegNumber = configuration.getString("registration_number.invalid").fold("")(x => x)
  lazy val invalidContactType = configuration.getString("contact_type.invalid").fold("")(x => x)
  lazy val invalidTemplate = configuration.getString("template_mapping.error").fold("")(x => x)
  lazy val invalidEmail = configuration.getString("email.invalid").fold("")(x => x)
  lazy val invalidUnknown = configuration.getString("unknown.failure").fold("")(x => x)
  lazy val invalidWErrorBuilder = configuration.getString("error_builder.failure").fold("")(x => x)
  lazy val invalidContentType = configuration.getString("content_type.invalid").fold("")(x => x)
  lazy val errorMaxLength = configuration.getString("error.maxLength").fold("")(x => x)
  lazy val errorExpectedBoolean = configuration.getString("error.expected.jsboolean").fold("")(x => x)
  lazy val errorExpectedString = configuration.getString("error.expected.jsstring").fold("")(x => x)
  lazy val invalidUri = configuration.getString("uri.invalid").fold("")(x => x)
  lazy val invalidApiType = configuration.getString("api_type.invalid").fold("")(x => x)
  lazy val getError = (key: String) => configuration.getString(key).fold(key)(x => x)
}

object ErrorConfig extends ErrorConfig
