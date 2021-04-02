package com.ananto.asyncgateway.verticles;

import com.ananto.asyncgateway.handlers.CallbackHandler;
import com.ananto.asyncgateway.handlers.ExampleHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Azizul Haque Ananto
 * @since 2/4/21
 */
public class WebVerticle extends AbstractVerticle {

  private static final int PORT = 8888;
  private static final long DEFAULT_REQUEST_TIMEOUT = 10000L;

  private static final Logger logger = LoggerFactory.getLogger(WebVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

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

    router.get("/async/:id").blockingHandler(new ExampleHandler());
    router.post("/callback").handler(new CallbackHandler());

    server.requestHandler(router).listen(PORT, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        logger.info("HTTP server started on port " + PORT);
      } else {
        startPromise.fail(http.cause());
      }
    });

  }

}
