package models
import akka.actor.ActorSystem
import akka.stream._
import play.api.libs.json.JsValue
import play.api.libs.json.JsValue.jsValueToJsLookup
import play.api.libs.ws.JsonBodyReadables.readableAsJson
import play.api.libs.ws._
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.Future
import scala.util.control.Breaks.break

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
    val wsClient1 = StandaloneAhcWSClient()
    callToRed(wsClient1)
      .andThen { case _ => wsClient1.close() }
      .andThen { case _ => system.terminate() }
//    val wsClient2 = StandaloneAhcWSClient()
//    println("-------")
//    callToRedTwoLights(wsClient2)
//      .andThen { case _ => wsClient2.close() }
//      .andThen { case _ => system.terminate() }
//    val wsClient3 = StandaloneAhcWSClient()
//    println("-------")
//    callToRedWithPath(wsClient3)
//      .andThen { case _ => wsClient3.close() }
//      .andThen { case _ => system.terminate() }
    //    call(wsClient)
    //      .andThen { case _ => wsClient.close() }
    //      .andThen { case _ => system.terminate() }

//    callToRed(wsClient)

//    callToRedWithPath(wsClient)

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
    def recur(id: Int): Future[Unit] = {
      val request = wsClient.url(s"$host/traffic-light/$id").get()
      request flatMap  { response =>
        val body = response.body[JsValue]
        val color= (body \ "color").as[String]
        println(body)
          if (color == "Red") {
            val stopID = (body \ "id").as[Int]
            println(s"We stop at $body")
            println((fromID to stopID).toList)
            break
          } else {
            recur(id + 1)
          }
      }
    }
    recur(fromID)

  }

  def callToRedTwoLights(wsClient: StandaloneWSClient): Future[Unit] = {
    val fromID1 = 1
    val fromID2 = 2
    val requestOne = wsClient.url(s"$host/get/$fromID1").get()
    val requestTwo = wsClient.url(s"$host/get/$fromID2").get()

    val futureResponse: Future[Unit] = for {
      responseOne <- requestOne.flatMap { response =>
        wsClient.url(s"$host/go-to-red/$fromID1").get().map { response2 =>
          val statusText: String = response.statusText
          val body = response.body[JsValue]
          val body2 = response2.body[JsValue]
          val stopID = (response2.body[JsValue] \ "id").as[Int]
          println(s"Got a response $statusText for request one:" + "\n" + s"We are going from $body" + "\n" + s"and stop at $body2")

        }
      }
      responseTwo <- requestTwo.flatMap { response =>
        wsClient.url(s"$host/go-to-red/$fromID2").get().map { response2 =>
          val statusText: String = response.statusText
          val body = response.body[JsValue]
          val body2 = response2.body[JsValue]
          val stopID = (response2.body[JsValue] \ "id").as[Int]
          println(s"Got a response $statusText for request two:" + "\n" + s"We are going from $body" + "\n" + s"and stop at $body2")
        }
      }
    } yield (responseOne, responseTwo)
    futureResponse
  }
}

