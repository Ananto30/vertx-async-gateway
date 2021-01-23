package com.ananto.asyncgateway;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Azizul Haque Ananto
 * @since 22/1/21
 */
public class DevApp {

  private static final String EVENT_BUS_CLUSTER_PUBLIC_HOST = "192.168.0.100"; // IMPORTANT! for dev, IP of your machine
  private static final int EVENT_BUS_CLUSTER_PUBLIC_PORT = 17001;

  private static final Logger logger = LoggerFactory.getLogger(DevApp.class);

  public static void main(String[] args) {
    /*
    IMPORTANT!!!
    The main method is only used in development mode, when run from intellij or calling the main method directly
     */
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
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
        logger.info("We now have a clustered event bus: " + eventBus);
      } else {
        logger.error("Failed: {}", res.cause(), res.cause());
      }
    });
  }
}
