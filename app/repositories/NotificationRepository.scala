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
import models.ContactTypes.ContactType
import org.mongodb.scala.bson.collection.Document
import org.mongodb.scala.model._
import org.mongodb.scala.result.DeleteResult
import play.api.Logging
import play.api.libs.json._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
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

  def deleteStatusNotification(registrationNumber: String): Future[DeleteResult]

  val maxRecords = 1000

}

class NotificationMongoRepositoryImpl @Inject()(mongoComponent: MongoComponent)(implicit ec: ExecutionContext) extends
  PlayMongoRepository[StatusNotification](
    mongoComponent = mongoComponent,
    collectionName = "statusNotification",
    domainFormat = StatusNotification.formats,
    indexes = Seq(
      IndexModel(Indexes.ascending("registrationNumber"), IndexOptions().name("registrationNumber").unique(true))
    )
  ) with NotificationRepository with Logging {

  override def findByRegistrationNumber(registrationNumber: String): Future[Option[StatusNotification]] = {

    val query = Filters.equal("registrationNumber", registrationNumber)
    Mdc.preservingMdc(
      collection
        .find(query)
        .first()
        .toFutureOption())
  }

  override def insertStatusNotification(statusNotification: StatusNotification): Future[Boolean] = {
  // upsert set as true so that we either update the record if it already exists or insert a new one if not

    val statusNotificationBson = Document("$set" -> Codecs.toBson(statusNotification))
    Mdc.preservingMdc {
      collection
        .updateOne(
          filter = Filters.equal("registrationNumber", Codecs.toBson(statusNotification.registrationNumber)),
          update = statusNotificationBson,
          options = UpdateOptions().upsert(true)
        )
        .toFuture()
    }.map {
        lastError =>
          logger.debug(s"[NotificationMongoRepository][insertByRegistrationNumber] : { statusNotification: $statusNotification," +
            s" result: ${lastError.wasAcknowledged()}")
          lastError.wasAcknowledged()
      }
  }

  override def deleteStatusNotification(registrationNumber: String): Future[DeleteResult] =
    Mdc.preservingMdc(
      collection
        .deleteOne(
          filter = Filters.equal("registrationNumber", Codecs.toBson(registrationNumber))
        )
        .toFuture()
    )

}
