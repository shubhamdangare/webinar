package com.knoldus.routes.contract.user

import play.api.libs.json.{Json, OWrites}

case class TokenResponse(response: String)

object TokenResponse {

  implicit val TokenResponseWrite: OWrites[TokenResponse] = Json.writes[TokenResponse]
}
