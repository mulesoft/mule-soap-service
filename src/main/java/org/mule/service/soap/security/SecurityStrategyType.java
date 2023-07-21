/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.security;


/**
 * Different types of {@link SecurityStrategyCxfAdapter} that specify when a strategy should be applied to a message.
 *
 * @since 1.0
 */
public enum SecurityStrategyType {

  /**
   * For configurations that should be applied to incoming (response) messages. This configuration type is used for decrypting and
   * verifying the signature of incoming messages.
   */
  INCOMING,

  /**
   * For configurations that should be applied to outgoing (request) messages. This configuration type is used for encryption,
   * signing and adding SAML, timestamp and username headers.
   */
  OUTGOING
}
