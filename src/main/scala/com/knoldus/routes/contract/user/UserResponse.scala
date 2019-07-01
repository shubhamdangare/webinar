package com.knoldus.routes.contract.user

import com.knoldus.common.user.UserDetails
import play.api.libs.json.{Json, OWrites}

case class UserResponse(
                         userName: String,
                         email: String,
                         name: String,
                         password: String
                       )

object UserResponse {

  implicit val UsesWrites: OWrites[UserResponse] = Json.writes[UserResponse]

  def toDomain(userResponse: UserDetails): UserResponse =
    UserResponse(userResponse.userName, userResponse.email,
      userResponse.name, userResponse.password)
}
