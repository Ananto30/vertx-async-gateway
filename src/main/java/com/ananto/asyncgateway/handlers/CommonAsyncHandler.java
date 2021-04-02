package com.ananto.asyncgateway.handlers;

import com.ananto.asyncgateway.store.Store;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @author Azizul Haque Ananto
 * @since 2/4/21
 */
public abstract class CommonAsyncHandler implements Handler<RoutingContext> {

  abstract String handleRequest(RoutingContext routingContext);

  @Override
  public void handle(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    response.putHeader("content-type", "application/json"); // Please remove this if all responses are not json
    var id = handleRequest(routingContext);
    Store.requests.put(id, response);
  }
}
