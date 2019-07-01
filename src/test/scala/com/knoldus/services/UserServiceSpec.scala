package com.knoldus.services

import java.time.Instant

import com.knoldus.common.user.UserDetails
import com.knoldus.dao.UserDao
import com.knoldus.domain.User
import com.knoldus.routes.contract.user.UserResponse
import com.knoldus.services.usermanagement.UserService
import com.knoldus.services.usermanagement.UserService.SignUpError
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.TableDrivenPropertyChecks
import org.apache.log4j.Logger
import org.mockito.Mockito.when

import scala.concurrent.Future

class UserServiceSpec extends WordSpec with ScalaFutures with Matchers with MockitoSugar with TableDrivenPropertyChecks {

  val conf = ConfigFactory.load()
  implicit val logger = Logger.getLogger(this.getClass)
  protected val dao: UserDao = mock[UserDao]

  val userService = new UserService(dao)

  def future[A](a: A): Future[A] = Future.successful(a)

  "UserService#addUser" should {
    "add user successfully" in {
      val person: User = User("shubhamD", "Lovelace@knoldus.in", "us", "17@adwZaa", Instant.now().toString)

      when(
        dao.getUIDCount(person.userName)
      ).thenReturn(0)
      when(
        dao.getCount(person.email)
      ).thenReturn(0)

      val userData: UserDetails = UserDetails(person.userName, person.email, person.name, person.password, person.created)

      when(
        dao.create(userData)
      ).thenReturn(1)

      whenReady(userService.registerUsers(person.userName, person.name, person.email, person.password))(_ shouldBe
        Right(UserResponse(person.userName, person.email, person.name, person.password)))
    }

    "email domain incorrect " in {
      val person: User = User("shubhamD", "Lovelace@gmail.com", "us", "17@adwZaa", Instant.now().toString)

      when(
        dao.getUIDCount(person.userName)
      ).thenReturn(0)
      when(
        dao.getCount(person.email)
      ).thenReturn(0)

      val userData: UserDetails = UserDetails(person.userName, person.email, person.name, person.password, person.created)

      when(
        dao.create(userData)
      ).thenReturn(1)

      whenReady(userService.registerUsers(person.userName, person.name, person.email, person.password))(_ shouldBe
        Left(SignUpError.OtherDomainUser))
    }


    "password is weak" in {
      val person: User = User("shubhamD", "Lovelace@gmail.com", "us", "11111111", Instant.now().toString)

      when(
        dao.getUIDCount(person.userName)
      ).thenReturn(0)
      when(
        dao.getCount(person.email)
      ).thenReturn(0)

      val userData: UserDetails = UserDetails(person.userName, person.email, person.name, person.password, person.created)

      when(
        dao.create(userData)
      ).thenReturn(1)

      whenReady(userService.registerUsers(person.userName, person.name, person.email, person.password))(_ shouldBe
        Left(SignUpError.PasswordNotMatch))
    }


    "email already taken" in {
      val person: User = User("shubhamD", "Lovelace@knoldus.com", "us", "17@adwZaa", Instant.now().toString)

      when(
        dao.getUIDCount(person.userName)
      ).thenReturn(0)
      when(
        dao.getCount(person.email)
      ).thenReturn(1)

      val userData: UserDetails = UserDetails(person.userName, person.email, person.name, person.password, person.created)

      when(
        dao.create(userData)
      ).thenReturn(1)

      whenReady(userService.registerUsers(person.userName, person.name, person.email, person.password))(_ shouldBe
        Left(SignUpError.EmailAlreadyTaken(person.email)))
    }

    "username already taken" in {
      val person: User = User("shubhamD", "Lovelace@knoldus.com", "us", "17@adwZaa", Instant.now().toString)

      when(
        dao.getUIDCount(person.userName)
      ).thenReturn(1)
      when(
        dao.getCount(person.email)
      ).thenReturn(1)

      val userData: UserDetails = UserDetails(person.userName, person.email, person.name, person.password, person.created)

      when(
        dao.create(userData)
      ).thenReturn(1)

      whenReady(userService.registerUsers(person.userName, person.name, person.email, person.password))(_ shouldBe
        Left(SignUpError.UserIdIsNotUnique))
    }

  }
}
