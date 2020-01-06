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

package repositories

import play.api.Logger
import play.api.libs.json.{JsString, Json, OFormat}
import javax.inject.Inject
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.ReadPreference

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class ViewedStatus(registrationNumber: Option[String], viewed: Option[Boolean])

object ViewedStatus {
  implicit val formats: OFormat[ViewedStatus] = Json.format[ViewedStatus]
}

trait NotificationViewedRepository {

  def findViewedStatusByRegistrationNumber(registrationNumber: String): Future[Option[ViewedStatus]]

  def insertViewedStatus(viewedStatus: ViewedStatus): Future[Boolean]

  def markAsViewed(registrationNumber: String): Future[WriteResult]

}

class NotificationViewedMongoRepositoryImpl @Inject()(mongo: ReactiveMongoComponent) extends
  ReactiveRepository[ViewedStatus, BSONObjectID]("viewedStatus", mongo.mongoConnector.db,
    ViewedStatus.formats, ReactiveMongoFormats.objectIdFormats) with NotificationViewedRepository {

  collection.indexesManager.ensure(Index(Seq("registrationNumber" -> IndexType.Ascending), name = Some("registrationNumber"), unique = true))

  override def findViewedStatusByRegistrationNumber(registrationNumber: String): Future[Option[ViewedStatus]] = {

    val query = Json.obj("registrationNumber" -> JsString(registrationNumber))
    collection.find(query).one[ViewedStatus](ReadPreference.primary)
  }

  // upsert set as true so that we either update the record if it already exists or insert a new one if not
  private def updateCore(viewedStatus: ViewedStatus) =
  collection.update(selector = Json.obj("registrationNumber" -> viewedStatus.registrationNumber),
    update = Json.obj("$set" -> Json.toJson(viewedStatus)),
    upsert = true)

  override def insertViewedStatus(viewedStatus: ViewedStatus): Future[Boolean] =
    updateCore(viewedStatus).map {
      lastError =>
        Logger.debug(s"[NotificationViewedRepository][insertViewedStatus] : { viewedStatus: $viewedStatus, " +
          s"result: ${lastError.ok}, errors: ${lastError.errmsg} }")
        lastError.ok
    }

  override def markAsViewed(registrationNumber: String): Future[WriteResult] =
    updateCore(ViewedStatus(Some(registrationNumber), Some(true)))

}

