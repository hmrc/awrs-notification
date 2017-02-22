/*
 * Copyright 2016 HM Revenue & Customs
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

package server

import play.modules.reactivemongo.MongoDbConnection
import uk.gov.hmrc.play.it.{ExternalServiceRunner, MongoMicroServiceEmbeddedServer, ServiceSpec}

class NotificationIntegrationServer(override val testName: String) extends MongoMicroServiceEmbeddedServer {
  val datastream = ExternalServiceRunner.runFromJar("datastream")
  val auth = ExternalServiceRunner.runFromJar("auth")
  val email = ExternalServiceRunner.runFromJar("email")
  val mailgun = ExternalServiceRunner.runFromJar("mailgun")
  override val externalServices = Seq(datastream, auth, email, mailgun)
}

class NotificationIntegrationISpec(testName: String) extends ServiceSpec {
  val server = new NotificationIntegrationServer(testName)

  protected implicit lazy val mongoConnection = new MongoDbConnection {}
  protected implicit lazy val db = mongoConnection.db
}