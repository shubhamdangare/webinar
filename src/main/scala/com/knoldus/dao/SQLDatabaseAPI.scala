package com.knoldus.dao

trait SQLDatabaseAPI {

  type parameter

  def create(parameter: parameter): Int

  def get(email: String, pass: String): Option[parameter]

  def getCount(email: String): Long

  def getUIDCount(id: String): Long

  def change(oldPassword: String, newPassword: String)

  def getUser(uid: String): Option[parameter]

}
