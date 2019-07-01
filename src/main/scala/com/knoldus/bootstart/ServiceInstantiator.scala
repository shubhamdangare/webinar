package com.knoldus.bootstart

import akka.actor.{ActorSystem, Scheduler}
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import com.knoldus.services.usermanagement.UserService
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

class ServiceInstantiator(
                           val conf: Config,
                           private val defaultValuesConfig: Config,
                           daoInisitator: DaoInisitator
                         )(
                           implicit system: ActorSystem,
                           val logger: LoggingAdapter,
                           materializer: ActorMaterializer
                         ) {

  implicit val ec: ExecutionContext = system.dispatcher
  implicit val scheduler: Scheduler = system.scheduler

  lazy val umService = new UserService(daoInisitator.userDatabaseDao)


}
