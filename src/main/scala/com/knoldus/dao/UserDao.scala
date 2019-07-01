package com.knoldus.dao

import com.knoldus.bootstart.ConnectionProvider
import com.knoldus.common.user.UserDetails
import com.knoldus.domain.User
import scalikejdbc._
import scalikejdbc.interpolation.SQLSyntax.count

class UserDao(dbConnection: ConnectionProvider) extends SQLDatabaseAPI {

  implicit val session = AutoSession
  type parameter = UserDetails

  override def create(parameter: UserDetails): Int = {
    val user = UserDetails.column
    withSQL {
      insert.into(UserDetails).columns(user.userName, user.email, user.name, user.password, user.created)
        .values(parameter.userName, parameter.email, parameter.name, parameter.password, parameter.created)
    }.update().apply()
  }

  override def get(email: String, password: String): Option[UserDetails] = {
    val userTable = UserDetails.syntax("m")
    withSQL {
      select.from(UserDetails as userTable).where.eq(userTable.email, email).and.eq(userTable.password, password)
    }.map(UserDetails(userTable.resultName)).single.apply()
  }

  override def getCount(email: String): Long = {
    val userTable = UserDetails.syntax("m")
    withSQL(
      select(count(userTable.email)).from(UserDetails as userTable).where.eq(userTable.email, email)
    ).map(rs => rs.long(1)).single.apply().get
  }

  def getUser(userName: String): Option[UserDetails] = {
    val userTable = UserDetails.syntax("m")
    withSQL {
      select.from(UserDetails as userTable).where.eq(userTable.userName, userName)
    }.map(UserDetails(userTable.resultName)).single.apply()
  }

  override def getUIDCount(userName: String): Long = {
    val userTable = UserDetails.syntax("m")
    withSQL(
      select(count(userTable.userName)).from(UserDetails as userTable).where.eq(userTable.userName, userName)
    ).map(rs => rs.long(1)).single.apply().get
  }

  override def change(oldPassword: String, newPassword: String): Unit = {
    update(UserDetails).set(
      UserDetails.column.password -> newPassword
    ).where.eq(UserDetails.column.password, oldPassword)
  }

}
