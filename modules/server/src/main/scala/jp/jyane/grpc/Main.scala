package jp.jyane.grpc

import io.grpc.{Server, ServerBuilder, Status}
import io.grpc.examples.helloworld.GreeterGrpc.Greeter
import io.grpc.examples.helloworld.{GreeterGrpc, HelloReply, HelloRequest}
import io.grpc.protobuf.services.ProtoReflectionService
import io.opencensus.contrib.grpc.metrics.RpcViews
import io.opencensus.contrib.zpages.ZPageHandlers
import io.opencensus.exporter.stats.prometheus.PrometheusStatsCollector
import io.opencensus.exporter.trace.logging.LoggingTraceExporter
import io.opencensus.exporter.trace.zipkin.ZipkinTraceExporter
import io.opencensus.trace.{Tracer, Tracing}
import io.prometheus.client.exporter.HTTPServer

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class GreeterService extends Greeter {
  override def sayHello(request: HelloRequest): Future[HelloReply] = {
    GreeterService.tracer.getCurrentSpan
    val name = request.name

    // simulate
    Thread.sleep(Random.nextInt(500))

    Random.nextInt(100) match {
      case x if x % 10 == 0 => Future.failed(Status.INTERNAL.asRuntimeException())
      case x if x % 5 == 0 => Future.failed(Status.INVALID_ARGUMENT.asRuntimeException())
      case _ => Future.successful(HelloReply("hello " + name))
    }
  }
}

object GreeterService {
  val tracer: Tracer = Tracing.getTracer
}

object Main {
  val server: Server = ServerBuilder.forPort(20080)
    .addService(GreeterGrpc.bindService(new GreeterService(), ExecutionContext.global))
    .addService(ProtoReflectionService.newInstance())
    .build()

  def start(): Unit = {
    RpcViews.registerAllViews()

    // zipkin
    ZipkinTraceExporter.createAndRegister("http://127.0.0.1:9411/api/v2/spans", "my-service")

    // trace logging
    LoggingTraceExporter.register()

    // zpage
    ZPageHandlers.startHttpServerAndRegisterAll(3000)

    // prometheus
    PrometheusStatsCollector.createAndRegister()
    new HTTPServer(9091, true)

    server.start()
  }

  def awaitTermination(): Unit = {
    server.awaitTermination()
  }

  def stop(): Unit = {
    try {
      server.shutdown()
    } catch {
      case e: InterruptedException =>
        e.printStackTrace()
        server.shutdownNow()
    }
  }

  def main(args: Array[String]): Unit = {
    start()
    awaitTermination()
    sys.addShutdownHook(stop())
  }
}
