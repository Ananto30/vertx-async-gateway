package com.ananto.asyncgateway.handlers;

import com.ananto.asyncgateway.Constants;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * @author Azizul Haque Ananto
 * @since 2/4/21
 */
public class CallbackHandler implements Handler<RoutingContext> {
  @Override
  public void handle(RoutingContext routingContext) {
    var body = routingContext.getBodyAsJson();
    routingContext.vertx().eventBus().publish(Constants.ASYNC_RESULT_EVENT.val, body);
    routingContext.response().end("OK");
  }
}
