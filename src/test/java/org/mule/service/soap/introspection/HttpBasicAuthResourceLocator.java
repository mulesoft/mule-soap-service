/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.introspection;

import static org.mule.runtime.http.api.HttpConstants.Method.GET;
import static org.mule.service.soap.server.BasicAuthHttpServer.PASSWORD;
import static org.mule.service.soap.server.BasicAuthHttpServer.USERNAME;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.lifecycle.Lifecycle;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
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
    HttpAuthentication auth = HttpAuthentication.basic(USERNAME, PASSWORD).build();
    try {
      HttpResponse response = httpClient.send(HttpRequest.builder().method(GET).uri(url).build(), 500, false, auth);
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
