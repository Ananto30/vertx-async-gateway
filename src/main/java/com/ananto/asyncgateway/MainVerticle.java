package com.ananto.asyncgateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class MainVerticle extends AbstractVerticle {

  private static final int PORT = 8888;
  private static final String ASYNC_RESULT_EVENT = "async.result";
  private static final long DEFAULT_REQUEST_TIMEOUT = 10000L;

  private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);


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
      logger.error(ctx.failure().getMessage(), ctx.failure());
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
        logger.info("HTTP server started on port " + PORT);
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
      logger.info(String.format("Event received | %s : %s%n", id, body));
      var entry = requests.get(id);
      if (entry != null) {
        if (entry.ended()) requests.remove(id);// timout request
        else entry.end(body.toString());
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

}

