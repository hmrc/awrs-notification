
package service

import helpers.{AssertionHelpers, IntegrationSpec}
import play.api.test.FutureAwaits
import repositories.{NotificationViewedMongoRepositoryImpl, ViewedStatus}

import scala.concurrent.ExecutionContext.Implicits.global

class NotificationViewedMongoRepositoryISpec extends IntegrationSpec with AssertionHelpers with FutureAwaits {
  class Setup {
    val repo: NotificationViewedMongoRepositoryImpl = app.injector.instanceOf[NotificationViewedMongoRepositoryImpl]

    await(repo.drop)
    await(repo.ensureIndexes)
  }

  override def additionalConfig(a: Map[String, Any]): Map[String, Any] = Map()

  "notificationRepository" should {
    "insertViewedStatus" should {
      "insert a viewed status" in new Setup {
        val viewedStatus: ViewedStatus = ViewedStatus(Some("regNumber"), Some(true))

        val res: Int = await {
          repo.insertViewedStatus(viewedStatus).flatMap {
            _ => repo.count
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
            _ => repo.count
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
