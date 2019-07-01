package com.knoldus.bootstart

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, handleExceptions, handleRejections}
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler, Route, ValidationRejection}
import com.knoldus.routes.contract.ErrorResponse
import com.typesafe.config.Config
import play.api.libs.json.{JsArray, JsObject, JsString, Json}

object BaseRoute {
  def seal(conf: Config)(route: Route): Route = {
    val exceptionHandler = ExceptionHandler {
      case e: Exception =>
        complete(HttpResponse(
          StatusCodes.InternalServerError,
          Nil,
          HttpEntity(
            ContentTypes.`application/json`,
            Json.stringify(Json.toJson(ErrorResponse(
              StatusCodes.InternalServerError.intValue,
              "Internal server error",
              errors = {
                if (conf.getBoolean("routes.debug-exceptions")) Seq(
                  JsObject(Seq(
                    "error" -> JsString(e.getClass.toString),
                    "message" -> JsString(e.getMessage),
                    "stackTrace" -> JsArray(e.getStackTrace.map(line => JsString(line.toString)))
                  ))
                )
                else Seq.empty
              }
            )))
          )
        ))
    }
    val rejectionHandler = RejectionHandler.newBuilder()
      .handle { case ValidationRejection(msg, _) =>
        complete((StatusCodes.BadRequest, "Invalid entry: " + msg))
      }
      .result()
      .withFallback(RejectionHandler.default)
      .mapRejectionResponse {
        // since all Akka default rejection responses are Strict this will handle all other rejections
        case res@HttpResponse(_, _, ent: HttpEntity.Strict, _) =>
          res.copy(entity = HttpEntity(ContentTypes.`application/json`, Json.stringify(Json.toJson(ErrorResponse(
            code = res.status.intValue,
            message = ent.data.utf8String
          )))))
        case x => x // pass through all other types of responses
      }

    (handleExceptions(exceptionHandler) & handleRejections(rejectionHandler)) (route)
  }
}
