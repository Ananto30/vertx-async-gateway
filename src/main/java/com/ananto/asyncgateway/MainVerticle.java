package com.ananto.asyncgateway;

import com.ananto.asyncgateway.verticles.AsyncVerticle;
import com.ananto.asyncgateway.verticles.WebVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    CompositeFuture.all(
      deploy(WebVerticle.class.getName()),
      deploy(AsyncVerticle.class.getName())
    ).onComplete(result -> {
      if (result.succeeded()) startPromise.complete();
      else startPromise.fail(result.cause());
    });
  }

  private Future<Void> deploy(String name) {
    final Promise<Void> promise = Promise.promise();
    vertx.deployVerticle(name, res -> {
      if (res.failed()) {
        logger.error("Failed to deploy verticle " + name);
        promise.fail(res.cause());
      } else {
        logger.info("Deployed verticle " + name);
        promise.complete();
      }
    });
    return promise.future();
  }

}

