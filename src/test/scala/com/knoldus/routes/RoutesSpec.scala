package com.knoldus.routes

import akka.http.scaladsl.model.{ContentTypes, HttpRequest, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.knoldus.bootstart.BaseRoute
import com.typesafe.config.{Config, ConfigFactory}
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.JsObject

trait RoutesSpec extends WordSpec
  with Matchers
  with ScalaFutures
  with MockitoSugar
  with ScalatestRouteTest
  with PlayJsonSupport { self =>

  val routes: Route

  def validateErrorResponse(response: JsObject): Unit =
    response.keys should contain allOf("code", "message")


  implicit class ExtendedHttpRequest(request: HttpRequest) {

    def check[T](body: => T): T = {
      check(ContentTypes.`application/json`)(body)
    }

    def check[T](expectedContentType: ContentType)(body: => T): T = {
      request ~> handledRoutes ~> self.check {
        contentType shouldBe expectedContentType
        body
      }
    }

    protected val conf: Config = ConfigFactory.load()
    private val handledRoutes = BaseRoute.seal(conf)(routes)

  }

}
