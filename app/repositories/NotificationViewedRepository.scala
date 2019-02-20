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

package repositories

import play.api.Logger
import play.api.libs.json.Json
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.api.{DefaultDB, ReadPreference}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.{ReactiveRepository, Repository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import reactivemongo.play.json.ImplicitBSONHandlers._

case class ViewedStatus(registrationNumber: Option[String], viewed: Option[Boolean])

object ViewedStatus {
  implicit val formats = Json.format[ViewedStatus]
}

trait NotificationViewedRepository extends Repository[ViewedStatus, BSONObjectID] {

  def findViewedStatusByRegistrationNumber(registrationNumber: String): Future[Option[ViewedStatus]]

  def insertViewedStatus(viewedStatus: ViewedStatus): Future[Boolean]

  def markAsViewed(registrationNumber: String): Future[WriteResult]

}

class NotificationViewedMongoRepository()(implicit mongo: () => DefaultDB)
  extends ReactiveRepository[ViewedStatus, BSONObjectID]("viewedStatus", mongo, ViewedStatus.formats)
    with NotificationViewedRepository {

  collection.indexesManager.ensure(Index(Seq("registrationNumber" -> IndexType.Ascending), name = Some("registrationNumber"), unique = true))

  override def findViewedStatusByRegistrationNumber(registrationNumber: String): Future[Option[ViewedStatus]] = {
    val tryResult = Try {
      collection.find(Json.obj("registrationNumber" -> registrationNumber)).cursor[ViewedStatus](ReadPreference.primary).collect[List]()
    }

    tryResult match {
      case Success(s) =>
        s.map { x =>
          Logger.debug(s"[NotificationViewedRepository][findViewedStatusByRegistrationNumber] : { registrationNumber : $registrationNumber, result: $x }")
          x.headOption
        }
      case Failure(f) =>
        Logger.debug(s"[NotificationViewedRepository][findViewedStatusByRegistrationNumber] : { registrationNumber : $registrationNumber, exception: ${f.getMessage} }")
        Future.successful(None)
    }

  }

  // upsert set as true so that we either update the record if it already exists or insert a new one if not
  private def updateCore(viewedStatus: ViewedStatus) =
  collection.update(selector = Json.obj("registrationNumber" -> viewedStatus.registrationNumber),
    update = Json.obj("$set" -> Json.toJson(viewedStatus)),
    upsert = true)

  override def insertViewedStatus(viewedStatus: ViewedStatus): Future[Boolean] =
    updateCore(viewedStatus).map {
      lastError =>
        Logger.debug(s"[NotificationViewedRepository][insertViewedStatus] : { viewedStatus: $viewedStatus, result: ${lastError.ok}, errors: ${lastError.errmsg} }")
        lastError.ok
    }

  override def markAsViewed(registrationNumber: String): Future[WriteResult] =
    updateCore(ViewedStatus(Some(registrationNumber), Some(true)))

}

object NotificationViewedRepository extends MongoDbConnection {

  private lazy val viewedStatusRepository = new NotificationViewedMongoRepository

  def apply(): NotificationViewedMongoRepository = viewedStatusRepository
}
