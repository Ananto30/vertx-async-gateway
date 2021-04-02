package com.ananto.asyncgateway;

/**
 * @author Azizul Haque Ananto
 * @since 2/4/21
 */


public enum Constants {
  ASYNC_RESULT_EVENT("ASYNC_RESULT_EVENT"),
  ASYNC_CALLBACK_IDENTIFIER("id");

  public final String val;

  Constants(String val) {
    this.val = val;
  }
}
