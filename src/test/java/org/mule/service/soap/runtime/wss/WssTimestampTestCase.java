/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.runtime.wss;

import static org.mule.test.allure.AllureConstants.WscFeature.WSC_EXTENSION;
import static java.util.Collections.singletonList;
import org.mule.runtime.extension.api.soap.security.SecurityStrategy;
import org.mule.runtime.extension.api.soap.security.TimestampSecurityStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(WSC_EXTENSION)
@Story("WSS")
public class WssTimestampTestCase extends AbstractWebServiceSecurityTestCase {

  @Override
  protected Interceptor buildInInterceptor() {
    final Map<String, Object> props = new HashMap<>();
    props.put("action", "Timestamp");
    return new WSS4JInInterceptor(props);
  }

  @Override
  protected Interceptor buildOutInterceptor() {
    final Map<String, Object> props = new HashMap<>();
    props.put("action", "Timestamp");
    return new WSS4JOutInterceptor(props);
  }

  @Override
  protected List<SecurityStrategy> getSecurityStrategies() {
    return singletonList(new TimestampSecurityStrategy(30));
  }
}
