package com.knoldus.common.user

import scalikejdbc._

case class UserDetails(
                        userName: String,
                        email: String ,
                        name: String,
                        password: String,
                        created: String
                      )

object UserDetails extends SQLSyntaxSupport[UserDetails] {
  override val tableName = "UserData"
  override val useSnakeCaseColumnName = false

  override val nameConverters = Map(
    "^userName$" -> "userName",
    "^name$" -> "name",
    "^email$" -> "email",
    "^password$" -> "password",
    "^created$" -> "created",
    "^isActive$" -> "isActive"
  )

  def apply(e: SyntaxProvider[UserDetails])(rs: WrappedResultSet): UserDetails = apply(e.resultName)(rs)

  def apply(e: ResultName[UserDetails])(rs: WrappedResultSet): UserDetails =
    new UserDetails(userName = rs.string(e.userName), email = rs.string(e.email),
      name = rs.string(e.name) ,password = rs.string(e.password),created = rs.string(e.created)
    )

}


