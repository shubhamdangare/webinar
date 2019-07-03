package com.knoldus.services.usermanagement

import java.time.Instant

import com.knoldus.common.user.UserDetails
import com.knoldus.dao.UserDao
import com.knoldus.routes.contract.user.UserResponse
import com.knoldus.services.usermanagement.UserService.{SignInError, SignUpError}
import com.knoldus.services.usermanagement.UserService.SignUpError.UserIdIsNotUnique
import scalikejdbc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class UserService(userDao: UserDao) {

  implicit val session: AutoSession.type = AutoSession

  def registerUsers(
                     userName: String,
                     name: String,
                     email: String,
                     password: String
                   ): Future[Either[SignUpError, UserResponse]] = Future {

    val emailFlag = email.endsWith("@knoldus.in") || email.endsWith("@knoldus.com")
    val pat = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,12}"

    if (password.matches(pat)) {
      if (emailFlag) {
        if (userDao.getUIDCount(userName) == 1) {
          Left(UserIdIsNotUnique)
        }
        else {
          if (userDao.getCount(email) == 0) {
            val userData = new UserDetails(userName, email, name, password, Instant.now().toString)
            userDao.create(userData)
            Right(UserResponse(userName, email, name, password))
          }
          else {
            Left(SignUpError.EmailAlreadyTaken(email))
          }
        }
      }
      else {
        Left(SignUpError.OtherDomainUser)
      }
    }
    else {
      Left(SignUpError.PasswordNotMatch)
    }

  }

  def signInUser(email: String, password: String): Future[Either[SignInError, UserDetails]] = Future {
    val userData = userDao.get(email, password)
    userData match {
      case Some(value) => Right(value)
      case None => Left(SignInError.InvalidCredentials)
    }
  }




}

object UserService {
  self =>

  sealed trait SignUpError

  object SignUpError {

    case class EmailAlreadyTaken(email: String) extends SignUpError

    case object UserIdIsNotUnique extends SignUpError

    case object OtherDomainUser extends SignUpError

    case object PasswordNotMatch extends SignUpError

  }

  sealed trait GetUserError

  object GetUserError {

    case object UserNotFound extends GetUserError

  }

  sealed trait SignInError

  object SignInError {

    case object InvalidCredentials extends SignInError

  }

  sealed trait UpdatePasswordError

  object UpdatePasswordError {

    case object InvalidOldPassword extends UpdatePasswordError

    case object InvalidToken extends UpdatePasswordError

  }

}

