
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
    await(repo.ensureIndexes)

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
