package models
import akka.actor.ActorSystem
import akka.stream._
import play.api.libs.json.JsValue
import play.api.libs.json.JsValue.jsValueToJsLookup
import play.api.libs.ws.JsonBodyReadables.readableAsJson
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

    //    call(wsClient)
    //      .andThen { case _ => wsClient.close() }
    //      .andThen { case _ => system.terminate() }

    callToRed(wsClient)
    callToRedWithPath(wsClient)
      .andThen { case _ => wsClient.close() }
      .andThen { case _ => system.terminate() }
    //    goToRed(wsClient)
    //      .andThen { case _ => wsClient.close() }
    //      .andThen { case _ => system.terminate() }
  }

  def call(wsClient: StandaloneWSClient): Future[Unit] = {

    wsClient.url(s"$host/all").get().map { response =>
      val statusText: String = response.statusText
      val body = response.body[String]
      println(s"Got a response $statusText: $body")
    }
  }

  def callToRed(wsClient: StandaloneWSClient): Future[Unit] = {
    val fromID = 1
    val response = wsClient.url(s"$host/get/$fromID").get()
    response flatMap { response =>
      wsClient.url(s"$host/go-to-red/$fromID").get().map { response2 =>
        val statusText: String = response.statusText
        val body = response.body[JsValue]
        val body2 = response2.body[JsValue]
        println(s"Got a response $statusText:" + "\n" + s"We are going from $body" + "\n" + s"and stop at $body2")
      }
    }
  }

  def callToRedWithPath(wsClient: StandaloneWSClient): Future[Unit] = {
    val fromID = 1
    val response = wsClient.url(s"$host/get/$fromID").get()
    response flatMap { response =>
      wsClient.url(s"$host/go-to-red/$fromID").get().map { response2 =>
        val statusText: String = response.statusText
        val body = response.body[JsValue]
        val body2 = response2.body[JsValue]
        val stopID = (response2.body[JsValue] \ "id").as[Int]
        println(s"Got a response $statusText:" + "\n" + s"We are going from $body" + "\n" + s"and stop at $body2")
        println((fromID to stopID).toList)
      }
    }
  }

//  def callToRedTwoLights(wsClient: StandaloneWSClient): Future[Unit] = {
//    val fromID1 = 1
//    val fromID2 = 2
//    val futureResponse: Future[StandaloneWSResponse] = for {
//      responseOne <- wsClient.url(s"$host/go-to-red/$fromID1").get()
//      responseTwo <- wsClient.url(s"$host/go-to-red/$fromID2").get()
//    } yield (responseOne.body, responseTwo.body)
//  }
  //  def goToRedFor2(wsClient: StandaloneWSClient): Future[Unit] = {
  //    val futureResponse: Future[StandaloneWSResponse] = for {
  //
  //
  //    }
  //  }
}

