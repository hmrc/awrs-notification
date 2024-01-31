/*
 * Copyright 2024 HM Revenue & Customs
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

package service

import helpers.{AssertionHelpers, IntegrationSpec}
import play.api.test.FutureAwaits
import repositories.{NotificationViewedMongoRepositoryImpl, ViewedStatus}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NotificationViewedMongoRepositoryISpec extends IntegrationSpec with AssertionHelpers with FutureAwaits {
  class Setup {
    val repo: NotificationViewedMongoRepositoryImpl = app.injector.instanceOf[NotificationViewedMongoRepositoryImpl]

    await(repo.collection.drop().head())
    await(repo.ensureIndexes())

    def getRepoCollectionCount: Future[Long] = repo.collection.countDocuments().toFuture()
  }

  override def additionalConfig(a: Map[String, Any]): Map[String, Any] = Map()

  "notificationRepository" should {
    "insertViewedStatus" should {
      "insert a viewed status" in new Setup {
        val viewedStatus: ViewedStatus = ViewedStatus(Some("regNumber"), Some(true))

        val res: Long = await {
          repo.insertViewedStatus(viewedStatus).flatMap {
            _ => getRepoCollectionCount
          }
        }
        res mustBe 1
      }
    }

    "findByRegistrationNumber" should {
      "find a viewed status" in new Setup {
        val regNumber = "regNumber"
        val viewedStatus: ViewedStatus = ViewedStatus(Some(regNumber), Some(true))

        val res: Option[ViewedStatus] = await {
          repo.insertViewedStatus(viewedStatus).flatMap {
            _ => repo.findViewedStatusByRegistrationNumber(regNumber)
          }
        }
        res.get.registrationNumber mustBe Some(regNumber)
      }
    }

    "markAsViewed" should {
      "marked a viewed status as viewed" in new Setup {
        val regNumber = "regNumber"
        val viewedStatus: ViewedStatus = ViewedStatus(Some(regNumber), Some(false))

        await {
          repo.insertViewedStatus(viewedStatus).flatMap {
            _ => getRepoCollectionCount
              .map {_ mustBe 1}
              .flatMap { _ =>
                repo.markAsViewed(regNumber)
              }
          }
        }

        val res: Option[ViewedStatus] = await {
          repo.findViewedStatusByRegistrationNumber(regNumber)
        }
        res.get.viewed mustBe Some(true)
      }
    }
  }
}
