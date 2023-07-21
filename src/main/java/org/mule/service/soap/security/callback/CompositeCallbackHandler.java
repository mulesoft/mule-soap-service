/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.security.callback;

import java.io.IOException;
import java.util.List;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Callback handler implementation that delegates the handle operation to a list of callback handlers. This allows to compose
 * multiple callback handler implementations to handle different types of callbacks.
 *
 * @since 1.0
 */
public class CompositeCallbackHandler implements CallbackHandler {

  private final List<CallbackHandler> callbackHandlers;

  public CompositeCallbackHandler(List<CallbackHandler> callbackHandlers) {
    this.callbackHandlers = callbackHandlers;
  }

  @Override
  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    for (CallbackHandler callbackHandler : callbackHandlers) {
      callbackHandler.handle(callbacks);
    }
  }
}
