package com.ananto.asyncgateway.handlers;

import com.ananto.asyncgateway.Constants;
import io.vertx.ext.web.RoutingContext;

/**
 * @author Azizul Haque Ananto
 * @since 2/4/21
 */
public class ExampleHandler extends CommonAsyncHandler {

  @Override
  String handleRequest(RoutingContext routingContext) {
    return routingContext.pathParam(Constants.ASYNC_CALLBACK_IDENTIFIER.val);
  }
}
