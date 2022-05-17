package edu.knoldus.ModulePattern2

import zio.{Has, UIO, URLayer}
import zio.console.Console
import zio.clock.Clock
case class LoggingLive(console: Console.Service, clock: Clock.Service) extends Logging {
  override def log(line: String): UIO[Unit] =
    for {
      current <- clock.currentDateTime.orDie
      _       <- console.putStrLn(current.toString + "--" + line).orDie
    } yield ()
}

object LoggingLive {
  val layer: URLayer[Has[Console.Service] with Has[Clock.Service], Has[Logging]] =
    (LoggingLive(_, _)).toLayer
}
