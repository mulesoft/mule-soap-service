/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
