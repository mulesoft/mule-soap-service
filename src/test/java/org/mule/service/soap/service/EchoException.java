/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.service;


import javax.xml.ws.WebFault;


@WebFault
public class EchoException extends Exception {

  private EchoFault echoFault;

  public EchoException(String message) {
    super(message);
    this.echoFault = new EchoFault();
    this.echoFault.setText(message);
  }

  public EchoFault getFaultInfo() {
    return echoFault;
  }
}
