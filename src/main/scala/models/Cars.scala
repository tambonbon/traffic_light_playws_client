package models
import akka.actor.ActorSystem
import akka.stream._
import play.api.libs.ws._
import play.api.libs.ws.ahc._

import scala.concurrent.Future

object Cars {

  import DefaultBodyReadables._

  import scala.concurrent.ExecutionContext.Implicits._
  val host = "http://localhost:9000"

  def main(args: Array[String]): Unit = {
    // Create Akka system for thread and streaming management
    implicit val system = ActorSystem()
    system.registerOnTermination {
      System.exit(0)
    }

    implicit val materializer = SystemMaterializer(system).materializer

    // Create the standalone WS client
    // no argument defaults to a AhcWSClientConfig created from
    // "AhcWSClientConfigFactory.forConfig(ConfigFactory.load, this.getClass.getClassLoader)"
    val wsClient = StandaloneAhcWSClient()

    call(wsClient)
      .andThen { case _ => wsClient.close() }
      .andThen { case _ => system.terminate() }

    goToRed(wsClient)
      .andThen { case _ => wsClient.close() }
      .andThen { case _ => system.terminate() }
  }

  def call(wsClient: StandaloneWSClient): Future[Unit] = {

    wsClient.url(s"$host/all").get().map { response =>
      val statusText: String = response.statusText
      val body = response.body[String]
      println(s"Got a response $statusText: $body")
    }
  }
  def callToRed(wsClient: StandaloneWSClient): Future[Unit] = {

    val request = wsClient.url(s"$host/all").get()

  }
  def goToRed(wsClient: StandaloneWSClient): Future[Unit] = {

    wsClient.url(s"$host/go-to-red/5").get().map { response =>
      val statusText: String = response.statusText
      val body = response.body[String]
      println(s"Got a response $statusText: $body")
    }
  }
  def goToRedFor2(wsClient: StandaloneWSClient): Future[Unit] = {
    val futureResponse: Future[StandaloneWSResponse] = for {
      responseOne <- wsClient.url(s"host/go-to-red/1").get()
      responseTwo <- wsClient.url(responseOne.body).get()

    }
  }
}

