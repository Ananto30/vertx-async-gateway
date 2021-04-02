package com.ananto.asyncgateway.verticles;

import com.ananto.asyncgateway.Constants;
import com.ananto.asyncgateway.store.Store;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Azizul Haque Ananto
 * @since 2/4/21
 */

public class AsyncVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(AsyncVerticle.class);

  @Override
  public void start() throws Exception {
    vertx.eventBus().consumer(Constants.ASYNC_RESULT_EVENT.val).handler(data -> {
      var body = (JsonObject) data.body();
      var id = body.getString(Constants.ASYNC_CALLBACK_IDENTIFIER.val);
      logger.info(String.format("Event received | %s : %s%n", id, body));
      var request = Store.requests.remove(id);
      if (request != null) request.end(body.toBuffer());
    });
  }

}
