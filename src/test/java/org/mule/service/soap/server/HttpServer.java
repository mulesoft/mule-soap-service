/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.server;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.xml.ws.Endpoint;

public class HttpServer {

  final Server httpServer;
  private final String defaultAddress;
  private final Interceptor in;
  private final Interceptor out;
  private final Object serviceInstance;

  public HttpServer(int port, Object serviceInstance) {
    this(port, null, null, serviceInstance);
  }

  public HttpServer(int port, Interceptor in, Interceptor out, Object serviceInstance) {
    this.httpServer = new Server(port);
    this.defaultAddress = "http://localhost:" + port + "/server";
    this.in = in;
    this.out = out;
    this.serviceInstance = serviceInstance;
    init();
  }

  protected void init() {
    try {
      ServletHandler servletHandler = new ServletHandler();
      httpServer.setHandler(servletHandler);
      CXFNonSpringServlet cxf = new CXFNonSpringServlet();
      ServletHolder servlet = new ServletHolder(cxf);
      servlet.setName("server");
      servlet.setForcedPath("/");
      servletHandler.addServletWithMapping(servlet, "/*");
      initializeServer(cxf);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  void initializeServer(CXFNonSpringServlet cxf) {
    try {
      httpServer.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    Bus bus = cxf.getBus();
    if (in != null) {
      bus.getInInterceptors().add(in);
    }
    if (out != null) {
      bus.getOutInterceptors().add(out);
    }
    BusFactory.setDefaultBus(bus);
    Endpoint.publish("/server", serviceInstance);
  }

  public void stop() throws Exception {
    httpServer.stop();
    httpServer.destroy();
  }

  public String getDefaultAddress() {
    return defaultAddress;
  }

  /**
   * Starts this server, which is stopped when the JVM is shutdown.
   * <p>
   * The expected arguments are:
   * <ul>
   * <li><b>portNumber</b>: The port where the HTTP server will listen for SOAP requests.</li>
   * <li><b>serviceClass</b>: Refer to @link {@link Endpoint#publish(String, Object)}</li>
   * </ul>
   */
  public static void main(String[] args) {
    final int port;
    final Object service;
    try {
      port = Integer.parseInt(args[0]);
      service = Class.forName(args[1]).newInstance();
    } catch (NumberFormatException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      e.printStackTrace();
      System.exit(-1);
      return;
    }

    final HttpServer server = new HttpServer(port, service);

    Runtime.getRuntime().addShutdownHook(new Thread() {

      @Override
      public void run() {
        try {
          server.stop();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    while (true) {
      try {
        Thread.sleep(10000);
      } catch (InterruptedException e) {
        System.exit(0);
      }
    }
  }
}
