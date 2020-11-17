package models
import akka.actor.ActorSystem
import akka.stream._
import play.api.libs.json.JsValue
import play.api.libs.json.JsValue.jsValueToJsLookup
import play.api.libs.ws.JsonBodyReadables.readableAsJson
import play.api.libs.ws._
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.Future

object Cars {

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
    callToRedTwoLights(wsClient1)
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

  }

  def callToRed(wsClient: StandaloneWSClient): Future[Unit] = {
    val fromID = 1
    def recur(id: Int): Future[Unit] = {
      val request = wsClient.url(s"$host/traffic-light/$id").get()
      // flatMap bc we are mapping Future[StandaloneWSRequest#Response](StandaloneWSRequest#Response => Future[Unit]): Future[Unit]
      // if it was Map -----> Future[Future[Unit]]
      request flatMap  { response =>
        val body = response.body[JsValue]
        val color= (body \ "color").as[String]
        println(body)
          if (color == "Red") {
            val stopID = (body \ "id").as[Int]
            println(s"We stop at $body")
            println((fromID to stopID).toList)
            Future.successful()
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

    def recur(id1: Int, id2: Int): Future[Unit] = {
      val requestOne = wsClient.url(s"$host/traffic-light/$id1").get()
      val requestTwo = wsClient.url(s"$host/traffic-light/$id2").get()

      requestOne flatMap { response1 =>
        requestTwo flatMap { response2 =>
          val body1 = response1.body[JsValue]
          val color1 = (body1 \ "color").as[String]
          val body2 = response2.body[JsValue]
          val color2 = (body2 \ "color").as[String]
          if (color1 == "Red") {
            println(s"Car 1 stops at $body1")
            Future.successful(println(""))
          }
          else if (color2 == "Red") {
            println(s"Car 2 stops at $body2")
            Future.successful(println(""))
          }
          else {
            recur(id1 + 1, id2 + 1)
          }
        }
      }
    }
    recur(fromID1, fromID2)
//    for {
//      one <- requestOne
//      body1 = one.body[JsValue]
//      color1 = (body1 \ "color").as[String]
//      if color1 equals "Red"
//      two <- requestTwo
//      body2 = (two.body[JsValue])
//      color2 = (body2 \ "color").as[String]
//      if color2 equals("Red")
//    } yield println(s"We stop at $body1, $body2")
  }


//  def callToRedTwoLights(wsClient: StandaloneWSClient): Future[Unit] = {
//    val fromID1 = 1
//    val fromID2 = 2
//    val requestOne = wsClient.url(s"$host/get/$fromID1").get()
//    val requestTwo = wsClient.url(s"$host/get/$fromID2").get()
//
//    val futureResponse: Future[Unit] = for {
//      responseOne <- requestOne.flatMap { response =>
//        wsClient.url(s"$host/go-to-red/$fromID1").get().map { response2 =>
//          val statusText: String = response.statusText
//          val body = response.body[JsValue]
//          val body2 = response2.body[JsValue]
//          val stopID = (response2.body[JsValue] \ "id").as[Int]
//          println(s"Got a response $statusText for request one:" + "\n" + s"We are going from $body" + "\n" + s"and stop at $body2")
//
//        }
//      }
//      responseTwo <- requestTwo.flatMap { response =>
//        wsClient.url(s"$host/go-to-red/$fromID2").get().map { response2 =>
//          val statusText: String = response.statusText
//          val body = response.body[JsValue]
//          val body2 = response2.body[JsValue]
//          val stopID = (response2.body[JsValue] \ "id").as[Int]
//          println(s"Got a response $statusText for request two:" + "\n" + s"We are going from $body" + "\n" + s"and stop at $body2")
//        }
//      }
//    } yield (responseOne, responseTwo)
//    futureResponse
//  }
}

