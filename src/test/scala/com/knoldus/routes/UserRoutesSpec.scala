package com.knoldus.routes

import java.time.Instant

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.knoldus.common.user.UserDetails
import com.knoldus.dao.UserDao
import com.knoldus.domain.User
import com.knoldus.routes.contract.user.UserResponse
import com.knoldus.services.usermanagement.UserService
import org.mockito.Mockito.when
import play.api.libs.json.Json

import scala.concurrent.Future

class UserRoutesSpec extends RoutesSpec {

  def future[A](a: A): Future[A] = Future.successful(a)

  private val service = mock[UserService]
  val routes: Route = new UserRoutes(service).route
  protected val dao: UserDao = mock[UserDao]

  "POST /register-user" should {

    "register user" in {

      val person: User = User("shubhamD", "Lovelace@knoldus.in", "shubham", "17@adwZaa", Instant.now().toString)

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

      when(service.registerUsers(person.userName, person.name, person.email, person.password)).thenReturn(
        future(Right(UserResponse(person.userName, person.email, person.name, person.password)))
      )

      val body = Json.parse(
        """
          |{
          | "userName": "shubhamD",
          | "email": "Lovelace@knoldus.in",
          | "name": "shubham",
          | "password": "17@adwZaa"
          |}
        """.stripMargin)

      Post("/register-user", body).check {
        status shouldBe StatusCodes.OK

      }
    }
  }

}
