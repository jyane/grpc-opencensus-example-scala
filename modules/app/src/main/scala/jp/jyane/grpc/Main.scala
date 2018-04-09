package jp.jyane.grpc

import io.grpc.{Server, ServerBuilder}
import io.grpc.examples.helloworld.GreeterGrpc.Greeter
import io.grpc.examples.helloworld.{GreeterGrpc, HelloReply, HelloRequest}
import io.grpc.protobuf.services.ProtoReflectionService
import io.opencensus.contrib.grpc.metrics.RpcViews
import io.opencensus.exporter.stats.prometheus.PrometheusStatsCollector
import io.prometheus.client.exporter.HTTPServer

import scala.concurrent.{ExecutionContext, Future}

class GreeterService extends Greeter {
  override def sayHello(request: HelloRequest): Future[HelloReply] = {
    val name = request.name
    Future.successful(HelloReply(message = name))
  }
}

object Main {
  val server: Server = ServerBuilder.forPort(20080)
    .addService(GreeterGrpc.bindService(new GreeterService(), ExecutionContext.global))
    .addService(ProtoReflectionService.newInstance())
    .build()

  def stop(): Unit = {
    try {
      server.shutdown()
    } catch {
      case _: Throwable =>
        server.shutdownNow()
    }
  }

  def main(args: Array[String]): Unit = {
    RpcViews.registerAllViews()
    PrometheusStatsCollector.createAndRegister()
    server.start()
    val prometheusServer: HTTPServer = new HTTPServer(9091, true)
    server.awaitTermination()
    sys.addShutdownHook(stop())
  }
}
