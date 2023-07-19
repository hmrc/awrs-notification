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

package repositories

import javax.inject.Inject
import org.mongodb.scala.bson.collection.Document
import org.mongodb.scala.model._
import org.mongodb.scala.result.UpdateResult
import play.api.Logging
import play.api.libs.json._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import uk.gov.hmrc.play.http.logging.Mdc

import scala.concurrent.{ExecutionContext, Future}

case class ViewedStatus(registrationNumber: Option[String], viewed: Option[Boolean])

object ViewedStatus {
  implicit val formats: OFormat[ViewedStatus] = Json.format[ViewedStatus]
}

trait NotificationViewedRepository {

  def findViewedStatusByRegistrationNumber(registrationNumber: String): Future[Option[ViewedStatus]]

  def insertViewedStatus(viewedStatus: ViewedStatus): Future[Boolean]

  def markAsViewed(registrationNumber: String): Future[UpdateResult]

}

class NotificationViewedMongoRepositoryImpl @Inject()(mongoComponent: MongoComponent)(implicit ec: ExecutionContext) extends
  PlayMongoRepository[ViewedStatus](
    mongoComponent = mongoComponent,
    collectionName = "viewedStatus",
    domainFormat = ViewedStatus.formats,
    indexes = Seq(
      IndexModel(Indexes.ascending("registrationNumber"), IndexOptions().name("registrationNumber").unique(true))
    )
  ) with NotificationViewedRepository with Logging {

  override def findViewedStatusByRegistrationNumber(registrationNumber: String): Future[Option[ViewedStatus]] = {

    val query = Filters.equal("registrationNumber", registrationNumber)
    Mdc.preservingMdc(
      collection
        .find(query)
        .first()
        .toFutureOption())
  }

  // upsert set as true so that we either update the record if it already exists or insert a new one if not
  private def updateCore(viewedStatus: ViewedStatus): Future[UpdateResult] = {

    val viewedStatusBson = Document("$set" -> Codecs.toBson(viewedStatus))

    collection
      .updateOne(
        filter = Filters.equal("registrationNumber", Codecs.toBson(viewedStatus.registrationNumber)),
        update = viewedStatusBson,
        options = UpdateOptions().upsert(true)
      )
      .toFuture()
  }

  override def insertViewedStatus(viewedStatus: ViewedStatus): Future[Boolean] =
    Mdc.preservingMdc {
      updateCore(viewedStatus)}.map {
        lastError =>
          logger.debug(s"[NotificationViewedRepository][insertViewedStatus] : { viewedStatus: $viewedStatus, " +
            s"result: ${lastError.wasAcknowledged()}")
          lastError.wasAcknowledged()
      }

  override def markAsViewed(registrationNumber: String): Future[UpdateResult] =
    Mdc.preservingMdc(updateCore(ViewedStatus(Some(registrationNumber), Some(true))))

}

