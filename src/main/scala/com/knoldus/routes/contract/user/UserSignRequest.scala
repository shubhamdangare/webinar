package com.knoldus.routes.contract.user

import play.api.libs.json.{Json, Reads}

case class UserSignRequest(
                        userName: String,
                        password: String
                      )

object UserSignRequest {

  implicit val UsesReads: Reads[UserSignRequest] = Json.reads[UserSignRequest]

}