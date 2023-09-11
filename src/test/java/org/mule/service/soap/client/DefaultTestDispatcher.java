/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/**
 *
 */
package org.mule.service.soap.client;

import static org.mule.runtime.core.internal.util.FunctionalUtils.safely;

import org.mule.runtime.extension.api.soap.message.DispatchingRequest;
import org.mule.runtime.extension.api.soap.message.DispatchingResponse;
import org.mule.runtime.extension.api.soap.message.MessageDispatcher;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration.Builder;
import org.mule.runtime.soap.api.message.dispatcher.DefaultHttpMessageDispatcher;
import org.mule.service.http.impl.service.HttpServiceImplementation;
import org.mule.tck.SimpleUnitTestSupportSchedulerService;

public class DefaultTestDispatcher implements MessageDispatcher {

  private final DefaultHttpMessageDispatcher delegate;
  private final HttpClient httpClient;

  public DefaultTestDispatcher() {
    HttpServiceImplementation httpService = new HttpServiceImplementation(new SimpleUnitTestSupportSchedulerService());
    this.httpClient = getHttpClient(httpService);
    this.delegate = new DefaultHttpMessageDispatcher(httpClient);
  }

  private HttpClient getHttpClient(HttpServiceImplementation httpService) {
    HttpClient client = httpService.getClientFactory().create(new Builder().setName("soap").build());
    client.start();
    return client;
  }

  @Override
  public DispatchingResponse dispatch(DispatchingRequest request) {
    return delegate.dispatch(request);
  }

  public void stop() {
    safely(httpClient::stop);
  }
}
