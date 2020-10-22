
package service

import helpers.{AssertionHelpers, IntegrationSpec}
import play.api.test.FutureAwaits
import repositories.{NotificationMongoRepositoryImpl, StatusNotification}

import scala.concurrent.ExecutionContext.Implicits.global

class NotificationRepositoryISpec extends IntegrationSpec with AssertionHelpers with FutureAwaits {
  class Setup {
    val repo: NotificationMongoRepositoryImpl = app.injector.instanceOf[NotificationMongoRepositoryImpl]

    await(repo.drop)
    await(repo.ensureIndexes)
  }

  override def additionalConfig(a: Map[String, Any]): Map[String, Any] = Map()

  "notificationRepository" should {
    "insertStatusNotification" should {
      "insert a status notification" in new Setup {
        val statusNotification: StatusNotification = StatusNotification(Some("regNumber"), None, None, None, None)

        val res: Int = await {
          repo.insertStatusNotification(statusNotification).flatMap {
            _ => repo.count
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
            _ => repo.count
              .map {_ mustBe 1}
              .flatMap { _ =>
                repo.deleteStatusNotification(regNumber)
              }
          }
        }

        await {
          repo.count map {_ mustBe 0}
        }
      }
    }
  }
}
