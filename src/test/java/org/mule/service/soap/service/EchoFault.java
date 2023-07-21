/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.service;

/**
 * Bean that is used as "detail" in the SOAP fault when an EchoException is thrown.
 */
public class EchoFault {

  private String text;

  public void setText(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

}
