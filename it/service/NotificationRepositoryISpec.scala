
package service

import helpers.{AssertionHelpers, IntegrationSpec}
import play.api.test.FutureAwaits
import repositories.{NotificationMongoRepositoryImpl, StatusNotification}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NotificationRepositoryISpec extends IntegrationSpec with AssertionHelpers with FutureAwaits {
  class Setup {
    val repo: NotificationMongoRepositoryImpl = app.injector.instanceOf[NotificationMongoRepositoryImpl]

    await(repo.collection.drop().head())
    await(repo.ensureIndexes)

    def getRepoCollectionCount: Future[Long] = repo.collection.countDocuments().toFuture()
  }

  override def additionalConfig(a: Map[String, Any]): Map[String, Any] = Map()

  "notificationRepository" should {
    "insertStatusNotification" should {
      "insert a status notification" in new Setup {
        val statusNotification: StatusNotification = StatusNotification(Some("regNumber"), None, None, None, None)

        val res: Long = await {
          repo.insertStatusNotification(statusNotification).flatMap {
            _ => getRepoCollectionCount
          }
        }

        res mustBe 1
      }
    }

    "findByRegistrationNumber" should {
      "find a status notification" in new Setup {
        val regNumber = "regNumber"
        val statusNotification: StatusNotification = StatusNotification(Some(regNumber), None, None, None, None)

        val res: Option[StatusNotification] = await {
          repo.insertStatusNotification(statusNotification).flatMap {
            _ => repo.findByRegistrationNumber(regNumber)
          }
        }

        res.get.registrationNumber mustBe Some(regNumber)
      }
    }

    "deleteStatusNotification" should {
      "delete a status notification" in new Setup {
        val regNumber = "regNumber"
        val statusNotification: StatusNotification = StatusNotification(Some(regNumber), None, None, None, None)

        await {
          repo.insertStatusNotification(statusNotification).flatMap {
            _ => getRepoCollectionCount
              .map {_ mustBe 1}
              .flatMap { _ =>
                repo.deleteStatusNotification(regNumber)
              }
          }
        }

        await {
          getRepoCollectionCount map {_ mustBe 0}
        }
      }
    }
  }
}
