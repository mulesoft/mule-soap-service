/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

/**
 *
 */
package org.mule.service.soap.introspection;

import static org.mule.runtime.http.api.HttpConstants.Method.GET;
import static org.mule.runtime.http.api.client.HttpAuthenticationType.BASIC;
import static org.mule.service.soap.server.BasicAuthHttpServer.PASSWORD;
import static org.mule.service.soap.server.BasicAuthHttpServer.USERNAME;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.lifecycle.Lifecycle;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration;
import org.mule.runtime.http.api.client.HttpRequestAuthentication;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.mule.runtime.soap.api.transport.TransportResourceLocator;
import org.mule.service.http.impl.service.HttpServiceImplementation;
import org.mule.tck.SimpleUnitTestSupportSchedulerService;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeoutException;

public class HttpBasicAuthResourceLocator implements TransportResourceLocator, Lifecycle {

  private final HttpServiceImplementation service = new HttpServiceImplementation(new SimpleUnitTestSupportSchedulerService());
  private final HttpClient httpClient;

  HttpBasicAuthResourceLocator() {
    this.httpClient = service.getClientFactory().create(new HttpClientConfiguration.Builder().setName("locator").build());
  }

  @Override
  public boolean handles(String url) {
    return url.startsWith("http");
  }

  @Override
  public InputStream getResource(String url) {
    HttpRequestAuthentication auth = new HttpRequestAuthentication(BASIC);
    auth.setUsername(USERNAME);
    auth.setPassword(PASSWORD);
    try {
      HttpResponse response = httpClient.send(HttpRequest.builder().setMethod(GET).setUri(url).build(), 500, false, auth);
      if (response.getStatusCode() == 401) {
        throw new RuntimeException("Unauthorized");
      }
      return response.getEntity().getContent();
    } catch (IOException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void stop() throws MuleException {
    service.stop();
  }

  @Override
  public void start() throws MuleException {
    httpClient.start();
  }

  @Override
  public void dispose() {}

  @Override
  public void initialise() throws InitialisationException {}
}
