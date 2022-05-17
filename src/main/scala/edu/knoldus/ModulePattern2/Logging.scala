package edu.knoldus.ModulePattern2

import zio.{Has, UIO, URIO, URLayer, ZIO}

trait Logging {
  def log(line: String): UIO[Unit]
}



object Logging {
  def log(line: String): URIO[Has[Logging], Unit] = ZIO.serviceWith[Logging](_.log(line))
}