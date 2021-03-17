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

import models.ContactTypes.ContactType
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
import uk.gov.hmrc.play.http.logging.Mdc
import scala.concurrent.{ExecutionContext, Future}

case class StatusNotification(registrationNumber: Option[String],
                              contactNumber: Option[String],
                              contactType: Option[ContactType],
                              status: Option[String],
                              storageDatetime: Option[String])

object StatusNotification {
  implicit val formats: OFormat[StatusNotification] = Json.format[StatusNotification]
}

trait NotificationRepository {

  def findByRegistrationNumber(registrationNumber: String): Future[Option[StatusNotification]]

  def insertStatusNotification(statusNotification: StatusNotification): Future[Boolean]

  def deleteStatusNotification(registrationNumber: String): Future[WriteResult]

  val maxRecords = 1000

}

class NotificationMongoRepositoryImpl @Inject()(mongo: ReactiveMongoComponent)(implicit ec: ExecutionContext) extends
  ReactiveRepository[StatusNotification, BSONObjectID]("statusNotification", mongo.mongoConnector.db,
    StatusNotification.formats, ReactiveMongoFormats.objectIdFormats) with NotificationRepository {

  Mdc.preservingMdc(collection.indexesManager.ensure(Index(Seq("registrationNumber" -> IndexType.Ascending), name = Some("registrationNumber"), unique = true)))

  override def findByRegistrationNumber(registrationNumber: String): Future[Option[StatusNotification]] = {

    val query = Json.obj("registrationNumber" -> JsString(registrationNumber))

    Mdc.preservingMdc(collection.find(query, None).one[StatusNotification](ReadPreference.primary))
  }

  override def insertStatusNotification(statusNotification: StatusNotification): Future[Boolean] =
  // upsert set as true so that we either update the record if it already exists or insert a new one if not
    Mdc.preservingMdc {
      collection.update(ordered = false).one(Json.obj("registrationNumber" -> statusNotification.registrationNumber),
        Json.obj("$set" -> Json.toJson(statusNotification)),
        upsert = true)}.map {
        lastError =>
          logger.debug(s"[NotificationMongoRepository][insertByRegistrationNumber] : { statusNotification: $statusNotification," +
            s" result: ${lastError.ok}, errors: ${lastError.errmsg} }")
          lastError.ok
      }

  override def deleteStatusNotification(registrationNumber: String): Future[WriteResult] =
    Mdc.preservingMdc(collection.delete().one(Json.obj("registrationNumber" -> registrationNumber)))

}
