package com.ananto.asyncgateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class MainVerticle extends AbstractVerticle {

  private static final int PORT = 8888;
  private static final String ASYNC_RESULT_EVENT = "async.result";
  private static final long DEFAULT_REQUEST_TIMEOUT = 10000L;
  private static final String EVENT_BUS_CLUSTER_PUBLIC_HOST = "192.168.0.100"; // IMPORTANT! for dev, IP of your machine
  private static final int EVENT_BUS_CLUSTER_PUBLIC_PORT = 17001;


  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    // keep track of the requests (actually response), to send them later when arrives in callback
    ConcurrentHashMap<String, HttpServerResponse> requests = new ConcurrentHashMap<>();

    HttpServer server = vertx.createHttpServer();

    Router router = Router.router(vertx);
    router.route().handler(LoggerHandler.create());
    router.route().handler(BodyHandler.create());
    router.route().handler(TimeoutHandler.create(DEFAULT_REQUEST_TIMEOUT)); // change default timeout to 10sec
    router.route().failureHandler(ctx -> {
      System.out.println(ctx.failure().getMessage());
      System.out.println(Arrays.toString(ctx.failure().getStackTrace()));
      ctx.next();
    });

    router.get("/health").handler(ctx -> {
      HttpServerResponse response = ctx.response();
      response.end("OK");
    });

    router.get("/async/:id").blockingHandler(ctx -> asyncHandler(ctx, requests));
    router.post("/callback/:id").handler(this::callbackHandler);

    server.requestHandler(router).listen(PORT, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port " + PORT);
        requestProcessor(requests);
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private void requestProcessor(ConcurrentHashMap<String, HttpServerResponse> requests) {
    vertx.eventBus().consumer(ASYNC_RESULT_EVENT).handler(data -> {
      var body = (JsonObject) data.body();
      var id = body.getString("id");
      System.out.printf("Event received | %s : %s%n", id, body);
      var entry = requests.get(id);
      if (entry != null) {
        entry.end(body.toString());
        requests.remove(id);
      }
    });
  }

  private void callbackHandler(io.vertx.ext.web.RoutingContext ctx) {
    var body = ctx.getBodyAsJson();
    var id = ctx.pathParam("id");
    body.put("id", id);
    vertx.eventBus().publish(ASYNC_RESULT_EVENT, body);
    ctx.response().end("OK");
  }

  private void asyncHandler(io.vertx.ext.web.RoutingContext ctx, ConcurrentHashMap<String, HttpServerResponse> requests) {
    HttpServerResponse response = ctx.response();
    response.putHeader("content-type", "application/json");
    var id = ctx.pathParam("id");
    requests.put(id, response);
  }


  public static void main(String[] args) {
    VertxOptions options = new VertxOptions()
      .setClusterManager(new HazelcastClusterManager()) // hazlecast is distributed in-memory data grid that can track the distributed clusters
      // for kubernetes - https://vertx.io/docs/vertx-hazelcast/java/#_configuring_for_kubernetes

      // this is where all the magic happens :D
      // the cluster implementation of the event bus allows different instances to talk with each other through tcp network
      // basically it's a distributed event bus now
      .setEventBusOptions(new EventBusOptions()
        // the most important part! public host can be different for each instance
        // for kubernetes we can use the service host most probably
        .setClusterPublicHost(EVENT_BUS_CLUSTER_PUBLIC_HOST)
        .setClusterPublicPort(EVENT_BUS_CLUSTER_PUBLIC_PORT)

      );
    Vertx.clusteredVertx(options, res -> {
      if (res.succeeded()) {
        Vertx vertx = res.result();

        // we can deploy multiple verticles using same vertx manager to utilize cores
        vertx.deployVerticle(new MainVerticle());
        vertx.deployVerticle(new MainVerticle());
        vertx.deployVerticle(new MainVerticle());
        vertx.deployVerticle(new MainVerticle());

        EventBus eventBus = vertx.eventBus();
        System.out.println("We now have a clustered event bus: " + eventBus);
      } else {
        System.out.println("Failed: " + res.cause());
      }
    });
  }
}

