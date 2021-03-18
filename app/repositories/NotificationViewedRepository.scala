/*
 * Copyright 2021 HM Revenue & Customs
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

import javax.inject.Inject
import play.api.libs.json.{JsObject, JsString, Json, OFormat}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.ReadPreference
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.play.http.logging.Mdc
import scala.concurrent.{ExecutionContext, Future}

case class ViewedStatus(registrationNumber: Option[String], viewed: Option[Boolean])

object ViewedStatus {
  implicit val formats: OFormat[ViewedStatus] = Json.format[ViewedStatus]
}

trait NotificationViewedRepository {

  def findViewedStatusByRegistrationNumber(registrationNumber: String): Future[Option[ViewedStatus]]

  def insertViewedStatus(viewedStatus: ViewedStatus): Future[Boolean]

  def markAsViewed(registrationNumber: String): Future[WriteResult]

}

class NotificationViewedMongoRepositoryImpl @Inject()(mongo: ReactiveMongoComponent)(implicit ec: ExecutionContext) extends
  ReactiveRepository[ViewedStatus, BSONObjectID]("viewedStatus", mongo.mongoConnector.db,
    ViewedStatus.formats, ReactiveMongoFormats.objectIdFormats) with NotificationViewedRepository {

  Mdc.preservingMdc(collection.indexesManager.ensure(Index(Seq("registrationNumber" -> IndexType.Ascending), name = Some("registrationNumber"), unique = true)))

  override def findViewedStatusByRegistrationNumber(registrationNumber: String): Future[Option[ViewedStatus]] = {

    val query = BSONDocument("registrationNumber" -> JsString(registrationNumber))
    Mdc.preservingMdc(collection.find(query, projection =
      Option.empty[JsObject]).one[ViewedStatus](ReadPreference.primary))
  }

  // upsert set as true so that we either update the record if it already exists or insert a new one if not
  private def updateCore(viewedStatus: ViewedStatus) =
    Mdc.preservingMdc(collection.update(ordered = false).one(Json.obj("registrationNumber" -> viewedStatus.registrationNumber),
    Json.obj("$set" -> Json.toJson(viewedStatus)),
    upsert = true))

  override def insertViewedStatus(viewedStatus: ViewedStatus): Future[Boolean] =
    Mdc.preservingMdc {
      updateCore(viewedStatus)}.map {
        lastError =>
          logger.debug(s"[NotificationViewedRepository][insertViewedStatus] : { viewedStatus: $viewedStatus, " +
            s"result: ${lastError.ok}, errors: ${lastError.errmsg} }")
          lastError.ok
      }

  override def markAsViewed(registrationNumber: String): Future[WriteResult] =
    Mdc.preservingMdc(updateCore(ViewedStatus(Some(registrationNumber), Some(true))))

}

