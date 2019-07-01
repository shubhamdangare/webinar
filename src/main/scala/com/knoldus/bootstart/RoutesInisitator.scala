package com.knoldus.bootstart

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.server.Directives.{concat, ignoreTrailingSlash}
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import com.knoldus.bootstart.BaseRoute._
import com.knoldus.routes.UserRoutes
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport

import scala.concurrent.ExecutionContext
class RoutesInisitator(
                        conf: Config,
                        services: ServiceInstantiator,
                        appUrl: String
                      )(
                        implicit val ec: ExecutionContext
                      ) extends PlayJsonSupport {

  private val umRoutes = new UserRoutes(services.umService)
  private val corsSettings = CorsSettings.defaultSettings
    .withAllowedMethods(scala.collection.immutable.Seq(GET, POST, PUT, HEAD, DELETE, OPTIONS))

  val routes: Route = cors(corsSettings) {
    seal(conf) {
      ignoreTrailingSlash {
        concat(
          umRoutes.route
        )
      }
    }
  }

}
