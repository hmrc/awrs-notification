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

package models

import play.api.libs.json.{Json, OFormat}
import repositories.ViewedStatus

case class ViewedStatusResponse(viewed: Boolean)

object ViewedStatusResponse {

  given convert: Conversion[ViewedStatus, ViewedStatusResponse] with
    def apply(viewedStatus: ViewedStatus) = ViewedStatusResponse(viewedStatus.viewed.get)

  given format: OFormat[ViewedStatusResponse] = Json.format[ViewedStatusResponse]

}
