package com.ananto.asyncgateway.store;

import io.vertx.core.http.HttpServerResponse;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Azizul Haque Ananto
 * @since 2/4/21
 */
public class Store {

  // keep track of the requests (actually response), to send them later when arrives in callback
  public static ConcurrentHashMap<String, HttpServerResponse> requests = new ConcurrentHashMap<>();
}
