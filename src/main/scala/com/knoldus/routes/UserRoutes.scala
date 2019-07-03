package com.knoldus.routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.knoldus.routes.contract.ErrorResponse
import com.knoldus.routes.contract.user.{TokenResponse, UserRequest}
import com.knoldus.services.usermanagement.UserService
import com.knoldus.services.usermanagement.UserService.SignInError.InvalidCredentials
import com.knoldus.services.usermanagement.UserService.SignUpError._
import com.knoldus.services.usermanagement.UserService._
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport


class UserRoutes(val userDBService: UserService) extends PlayJsonSupport {

  val route: Route = {
    path("sign-in") {
      (get & parameters('user.as[String], 'pass.as[String])) { (name, pass) => {
        onSuccess(userDBService.signInUser(name, pass)) {
          case Right(user) => complete(TokenResponse((user.userName, user.name, user.email).toString()))
          case Left(error) => complete(translateError(error))
        }
      }
      }
    }~ path("register-user") {
      (post & entity(as[UserRequest])) { userRequest =>
        val data = userDBService.registerUsers(
          userRequest.userName,
          userRequest.name,
          userRequest.email,
          userRequest.password
        )
        onSuccess(data) {
          case Right(user) => complete(user)
          case Left(error) => complete(translateError(error))
        }
      }
    }
  }

  private def translateError(error: GetUserError): (StatusCodes.ClientError, ErrorResponse) = {
    error match {
      case GetUserError.UserNotFound =>
        StatusCodes.BadRequest -> ErrorResponse(StatusCodes.BadRequest.intValue, "User Not Found")
    }
  }

  private def translateError(error: SignUpError): (StatusCodes.ClientError, ErrorResponse) = {
    error match {
      case EmailAlreadyTaken(email) =>
        StatusCodes.BadRequest -> ErrorResponse(StatusCodes.BadRequest.intValue,
          s"User with email $email already registered")
      case UserIdIsNotUnique =>
        StatusCodes.BadRequest -> ErrorResponse(StatusCodes.BadRequest.intValue,
          s"UserId is Not Unique")
      case OtherDomainUser =>
        StatusCodes.BadRequest -> ErrorResponse(StatusCodes.BadRequest.intValue,
          s"Other Domain User")
      case PasswordNotMatch =>
        StatusCodes.BadRequest -> ErrorResponse(StatusCodes.BadRequest.intValue,
          s"Password Pattern Not Match")
    }
  }

  private def translateError(signInError: SignInError): (StatusCodes.ClientError, ErrorResponse) = {
    signInError match {
      case InvalidCredentials =>
        StatusCodes.BadRequest -> ErrorResponse(StatusCodes.BadRequest.intValue, "Invalid credentials")
    }
  }

  private def translateError(error: UpdatePasswordError): (StatusCodes.ClientError, ErrorResponse) = {
    error match {
      case UpdatePasswordError.InvalidOldPassword =>
        StatusCodes.BadRequest -> ErrorResponse(StatusCodes.BadRequest.intValue, "Invalid old password")
      case UpdatePasswordError.InvalidToken =>
        StatusCodes.BadRequest -> ErrorResponse(StatusCodes.BadRequest.intValue, "Invalid token")
    }
  }

}
