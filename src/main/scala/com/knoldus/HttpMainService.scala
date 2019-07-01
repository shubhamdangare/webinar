package com.knoldus

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.knoldus.bootstart.{DBConnection, DaoInisitator, RoutesInisitator, ServiceInstantiator}
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object HttpMainService extends App {

  val conf = ConfigFactory.load()
  val actorSystem = ActorSystem("Webinar-Actor-System")
  val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)
  implicit val logger: LoggingAdapter = Logging(actorSystem, "Webinar")

  implicit val executionContext: ExecutionContextExecutor = global

  try {
    val baseAppUrl = conf.getString("base-app-url")
    val publicHttpServerConfig = conf.getConfig("http")
    val appHttpPrefix = publicHttpServerConfig.getString("prefix")
    val appUrl = s"$baseAppUrl/$appHttpPrefix"

    val publicHttpServer = new HttpServer(publicHttpServerConfig)(
      system = actorSystem,
      executionContext = actorSystem.dispatcher,
      materializer = materializer,
      logger = logger
    )
    val tabularConnectionInstantiator = new DBConnection(conf.getConfig("tabular-storage"))

    val daoInisitator: DaoInisitator = new DaoInisitator(tabularConnectionInstantiator.connectionProvider)

    val services = new ServiceInstantiator(
      conf = conf,
      defaultValuesConfig = ConfigFactory.load("default-values"),
      daoInisitator
    )(actorSystem, logger, materializer)

    val publicRoutes = new RoutesInisitator(conf, services, appUrl)(actorSystem.dispatcher).routes

    val publicServerBinding: Future[Http.ServerBinding] = publicHttpServer.start(publicRoutes)
    scala.sys.addShutdownHook {
      val cleanup = for {
        _ <- publicServerBinding.flatMap(_.unbind())
          .andThen {
            case Success(_) => logger.info("Has unbounded public http server.")
            case Failure(ex) => logger.error(ex, "Has failed to unbind http server.")
          }
        _ <- actorSystem.terminate()
          .andThen {
            case Success(_) => logger.info(s"Actor system $actorSystem has been terminated.")
            case Failure(ex) => logger.error(ex, s"Has failed to stop actor system $actorSystem")
          }
      } yield ()

      Await.result(cleanup, 1 minute)
    }
  } catch {
    case e: Throwable =>
      logger.error(e, "Error starting application:")
      Await.result(actorSystem.terminate(), 30 second)
  }
}
