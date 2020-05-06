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

package models

import scala.util.matching.Regex

object RegularExpressions extends RegularExpressions

trait RegularExpressions {

  val registrationRegex = "^X[A-Z]AW00000[0-9]{6}$"

  val nameRegex: Regex = "^[A-Za-z0-9 ]{1,140}$".r

  val statusRegex: Regex = "0[4-9]|10".r

  val contactNoRegex: Regex = "[0-9]{12}".r

  val emailRegex: Regex = """(^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$)""".r

}
