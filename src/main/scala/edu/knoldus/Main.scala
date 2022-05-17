package edu.knoldus

import edu.knoldus.DatabaseService._
import edu.knoldus.LoggerService._
import edu.knoldus.UserRepo.Users
import zio.ZLayer
import zio._
import zio.console._

import java.util.UUID

object LoggerService {
  type Logger = Has[Logger.Service]

  object Logger {
    trait Service {
      def log(line: String): UIO[Unit]
    }

    val any: ZLayer[Logger, Nothing, Logger] =
      ZLayer.requires[Logger]

    val live: Layer[Nothing, Has[Service]] = ZLayer.succeed {
      (line: String) => {
        putStrLn(line).provideLayer(Console.live).orDie
      }
    }
  }

  def log(line: => String): ZIO[Logger, Throwable, Unit] =
    ZIO.accessM(_.get.log(line))
}


case class User(id: String, name: String)

object DatabaseService {
  type Database = Has[Database.Service]

  object Database {
    trait Service {
      def getUser(id: String): Task[User]
    }

    val any: ZLayer[Database, Nothing, Database] =
      ZLayer.requires[Database]

    val live: Layer[Nothing, Has[Service]] = ZLayer.succeed {
      (id: String) => {
        Task(User(id, "Akash"))
      }
    }
  }

  def getUser(id: => String): ZIO[Database, Throwable, User] =
    ZIO.accessM(_.get.getUser(id))
}


object UserRepo {
  type Users = Has[Users.Service]

  object Users {
    trait Service {
      def getUser(id: String): Task[Unit]
    }

    val any: ZLayer[Users, Nothing, Users] =
      ZLayer.requires[Users]

    val live: ZLayer[Has[Database.Service] with Has[Logger.Service], Nothing, Has[Service]] =
      ZLayer.fromServices[Database.Service, Logger.Service, Service] { (database, logger) =>
        new Service {
          override def getUser(id: String): Task[Unit] = for {
            user <- database.getUser(id)
            _ <- logger.log(s"Hello $user")
          } yield ()
        }
      }
  }

  def getUser(id: => String): ZIO[Users, Throwable, Unit] =
    ZIO.accessM(_.get.getUser(id))
}


object Main extends zio.App {

  val program: ZIO[Users, Throwable, Unit] = for {
    id <- ZIO(UUID.randomUUID().toString)
    _ <- UserRepo.getUser(id)
  } yield ()

  val horizontalComposeLayer: ZLayer[Any, Nothing, Has[Database.Service] with Has[Logger.Service]] = Database.live ++ Logger.live

  val combinedLayer: ZLayer[Any, Nothing, Has[Users.Service]] = horizontalComposeLayer >>> Users.live

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = program.provideSomeLayer(combinedLayer).exitCode
}