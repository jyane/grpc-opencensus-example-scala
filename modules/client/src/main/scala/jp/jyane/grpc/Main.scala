package jp.jyane.grpc

import io.grpc.{ManagedChannelBuilder, StatusRuntimeException}
import io.grpc.examples.helloworld.{GreeterGrpc, HelloRequest}
import io.opencensus.trace.Tracing
import io.opencensus.trace.samplers.Samplers

import scala.concurrent.Await
import scala.concurrent.duration._

object Main {
  val channel = ManagedChannelBuilder.forAddress("localhost", 20080)
    .usePlaintext()
    .build()

  val tracer = Tracing.getTracer

  def stop(): Unit = {
    try {
      channel.shutdown()
    } catch {
      case e: InterruptedException =>
        e.printStackTrace()
        channel.shutdownNow()
    }
  }

  def main(args: Array[String]): Unit = {
    val stub = GreeterGrpc.stub(channel)

    val spanBuilder = tracer.spanBuilder("hello-span")
      .setRecordEvents(true)
      .setSampler(Samplers.alwaysSample())
    (1 to 100).map { x =>
      try {
        spanBuilder.startScopedSpan()
        Await.result(stub.sayHello(HelloRequest("req" + x.toString)), 3.seconds)
      } catch {
        case e: StatusRuntimeException =>
          e.printStackTrace()
      }
    }

    stop()
  }
}
