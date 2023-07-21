/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.security.callback;

import org.apache.wss4j.common.ext.WSPasswordCallback;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.stream.Stream;


/**
 * Abstract implementation of {@link CallbackHandler} that only handles instances of {@link WSPasswordCallback} with a specific
 * usage.
 *
 * @since 1.0
 */
public class WSPasswordCallbackHandler implements CallbackHandler {

  private final int usage;
  private final Consumer<WSPasswordCallback> handler;

  /**
   * Creates a new instance.
   *
   * @param usage   A constant from {@link WSPasswordCallback} indicating the usage of this callback.
   * @param handler {@link Consumer} that handles a {@link WSPasswordCallback}. This function will be called with the password
   *                callback that matches the {@code usage} also provided.
   */
  public WSPasswordCallbackHandler(int usage, Consumer<WSPasswordCallback> handler) {
    this.usage = usage;
    this.handler = handler;
  }

  @Override
  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    Stream.of(callbacks)
        .filter(callback -> callback instanceof WSPasswordCallback && ((WSPasswordCallback) callback).getUsage() == usage)
        .forEach(callback -> handler.accept((WSPasswordCallback) callback));
  }
}
