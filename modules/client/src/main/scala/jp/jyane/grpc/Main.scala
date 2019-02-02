package jp.jyane.grpc

import io.grpc.{ManagedChannelBuilder, StatusRuntimeException}
import io.grpc.examples.helloworld.{GreeterGrpc, HelloRequest}

import scala.concurrent.Await
import scala.concurrent.duration._

object Main {
  def main(args: Array[String]): Unit = {
    val channel = ManagedChannelBuilder.forAddress("localhost", 20080)
      .usePlaintext()
      .build()

    val stub = GreeterGrpc.stub(channel)

    try {
      Await.result(stub.sayHello(HelloRequest("jyane")), 1.seconds)
    } catch {
      case e: StatusRuntimeException =>
        e.printStackTrace()
    }
  }
}
