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

package repositories

import models.ContactTypes.ContactType
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

case class StatusNotification(registrationNumber: Option[String],
                              contactNumber: Option[String],
                              contactType: Option[ContactType],
                              status: Option[String],
                              storageDatetime: Option[String])

object StatusNotification {
  implicit val formats = Json.format[StatusNotification]
}

trait NotificationRepository extends Repository[StatusNotification, BSONObjectID] {

  def findByRegistrationNumber(registrationNumber: String): Future[Option[StatusNotification]]

  def insertStatusNotification(statusNotification: StatusNotification): Future[Boolean]

  def deleteStatusNotification(registrationNumber: String): Future[WriteResult]

}

class NotificationMongoRepository()(implicit mongo: () => DefaultDB)
  extends ReactiveRepository[StatusNotification, BSONObjectID]("statusNotification", mongo, StatusNotification.formats)
    with NotificationRepository {

  collection.indexesManager.ensure(Index(Seq("registrationNumber" -> IndexType.Ascending), name = Some("registrationNumber"), unique = true))

  override def findByRegistrationNumber(registrationNumber: String): Future[Option[StatusNotification]] = {
    val tryResult = Try {
      collection.find(Json.obj("registrationNumber" -> registrationNumber)).cursor[StatusNotification](ReadPreference.primary).collect[List]()
    }

    tryResult match {
      case Success(s) =>
        s.map { x =>
          Logger.debug(s"[NotificationMongoRepository][findByRegistrationNumber] : { registrationNumber : $registrationNumber, result: $x }")
          x.headOption
        }
      case Failure(f) =>
        Logger.debug(s"[NotificationMongoRepository][findByRegistrationNumber] : { registrationNumber : $registrationNumber, exception: ${f.getMessage} }")
        Future.successful(None)
    }
  }

  override def insertStatusNotification(statusNotification: StatusNotification): Future[Boolean] =
  // upsert set as true so that we either update the record if it already exists or insert a new one if not
    collection.update(selector = Json.obj("registrationNumber" -> statusNotification.registrationNumber),
      update = Json.obj("$set" -> Json.toJson(statusNotification)),
      upsert = true).map {
      lastError =>
        Logger.debug(s"[NotificationMongoRepository][insertByRegistrationNumber] : { statusNotification: $statusNotification, result: ${lastError.ok}, errors: ${lastError.errmsg} }")
        lastError.ok
    }

  override def deleteStatusNotification(registrationNumber: String): Future[WriteResult] =
    collection.remove(query = Json.obj("registrationNumber" -> registrationNumber))

}

object NotificationRepository extends MongoDbConnection {

  private lazy val notificationRepository = new NotificationMongoRepository

  def apply(): NotificationMongoRepository = notificationRepository
}
