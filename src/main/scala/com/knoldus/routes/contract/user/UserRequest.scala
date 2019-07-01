package com.knoldus.routes.contract.user

import play.api.libs.json.{Json, Reads}

case class UserRequest(
                        userName: String,
                        email: String,
                        name: String,
                        password: String
                      )

object UserRequest {

  implicit val UsesReads: Reads[UserRequest] = Json.reads[UserRequest]

}