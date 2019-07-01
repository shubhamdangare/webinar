package com.knoldus.bootstart

import com.knoldus.dao.UserDao

class DaoInisitator(connectionProvider: ConnectionProvider) {

  lazy val userDatabaseDao: UserDao = new UserDao(connectionProvider)

}
