package edu.knoldus.ModulePattern2
import zio.clock.Clock
import zio.{ExitCode, Has, URIO, ZIO, ZLayer}
import zio.console._

object MainApp extends zio.App {

  val program: ZIO[Has[Logging], Nothing, Unit] = for {
    _ <- Logging.log("Hello world")
  }  yield ()

  val env: ZLayer[Any, Nothing, Console with Clock] = Console.live ++ zio.clock.Clock.live
  val env2: ZLayer[Any, Nothing, Has[Logging]] = env >>> LoggingLive.layer
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = program.provideSomeLayer(env2).exitCode
}
