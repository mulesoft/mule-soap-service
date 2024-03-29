/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.runtime.wss;

import static org.mule.test.allure.AllureConstants.WscFeature.WSC_EXTENSION;
import static java.util.Collections.singletonList;

import static org.mule.runtime.extension.api.soap.security.PasswordType.TEXT;
import org.mule.runtime.extension.api.soap.security.SecurityStrategy;
import org.mule.runtime.extension.api.soap.security.UsernameTokenSecurityStrategy;
import org.mule.service.soap.service.ServerPasswordCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(WSC_EXTENSION)
@Story("WSS")
public class WssUsernameTestCase extends AbstractWebServiceSecurityTestCase {

  @Override
  protected Interceptor buildInInterceptor() {
    final Map<String, Object> props = new HashMap<>();
    props.put("action", "UsernameToken");
    props.put("passwordCallbackClass", ServerPasswordCallback.class.getName());
    return new WSS4JInInterceptor(props);
  }

  @Override
  protected List<SecurityStrategy> getSecurityStrategies() {
    return singletonList(new UsernameTokenSecurityStrategy("admin", "textPassword", TEXT, true, true));
  }
}
