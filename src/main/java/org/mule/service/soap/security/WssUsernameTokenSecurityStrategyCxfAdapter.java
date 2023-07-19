/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.security;

import static java.util.Optional.of;
import static org.apache.wss4j.common.ConfigurationConstants.ADD_USERNAMETOKEN_CREATED;
import static org.apache.wss4j.common.ConfigurationConstants.ADD_USERNAMETOKEN_NONCE;
import static org.apache.wss4j.common.ConfigurationConstants.USER;
import static org.apache.wss4j.common.ext.WSPasswordCallback.USERNAME_TOKEN;
import static org.apache.wss4j.dom.WSConstants.CREATED_LN;
import static org.apache.wss4j.dom.WSConstants.NONCE_LN;
import static org.apache.wss4j.dom.message.token.UsernameToken.PASSWORD_TYPE;

import org.mule.runtime.extension.api.soap.security.PasswordType;
import org.mule.runtime.extension.api.soap.security.UsernameTokenSecurityStrategy;
import org.mule.service.soap.security.callback.WSPasswordCallbackHandler;
import com.google.common.collect.ImmutableMap;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import java.util.Map;
import java.util.StringJoiner;


/**
 * Provides the capability to authenticate using Username and Password with a SOAP service by adding the UsernameToken element in
 * the SOAP request.
 *
 * @since 1.0
 */
public class WssUsernameTokenSecurityStrategyCxfAdapter implements SecurityStrategyCxfAdapter {

  /**
   * The username required to authenticate with the service.
   */
  private String username;

  /**
   * The password for the provided username required to authenticate with the service.
   */
  private String password;

  /**
   * A {@link PasswordType} which qualifies the {@link #password} parameter.
   */
  private PasswordType passwordType;

  /**
   * Specifies a if a cryptographically random nonce should be added to the message.
   */
  private boolean addNonce;

  /**
   * Specifies if a timestamp should be created to indicate the creation time of the message.
   */
  private boolean addCreated;

  public WssUsernameTokenSecurityStrategyCxfAdapter(UsernameTokenSecurityStrategy usernameToken) {
    this.addCreated = usernameToken.isAddCreated();
    this.addNonce = usernameToken.isAddNonce();
    this.password = usernameToken.getPassword();
    this.username = usernameToken.getUsername();
    this.passwordType = usernameToken.getPasswordType();
  }

  public SecurityStrategyType securityType() {
    return SecurityStrategyType.OUTGOING;
  }

  @Override
  public java.util.Optional<WSPasswordCallbackHandler> buildPasswordCallbackHandler() {
    return of(new WSPasswordCallbackHandler(USERNAME_TOKEN,
                                            cb -> {
                                              if (cb.getIdentifier().equals(username)) {
                                                cb.setPassword(password);
                                              }
                                            }));
  }

  @Override
  public String securityAction() {
    return WSHandlerConstants.USERNAME_TOKEN;
  }

  @Override
  public Map<String, Object> buildSecurityProperties() {
    return ImmutableMap.<String, Object>builder()
        .put(USER, username)
        .put(PASSWORD_TYPE, passwordType.getType())
        .put(ADD_USERNAMETOKEN_NONCE, String.valueOf(addNonce))
        .put(ADD_USERNAMETOKEN_CREATED, String.valueOf(addNonce)).build();
  }
}
