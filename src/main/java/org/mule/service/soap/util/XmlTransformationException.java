/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.util;


/**
 * {@link Exception} implementation that aims to be thrown when an XML transformation problem occur.
 *
 * @since 1.0
 */
public class XmlTransformationException extends Exception {

  XmlTransformationException(String message, Throwable cause) {
    super(message, cause);
  }
}
