/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.service;

import javax.jws.WebService;
import javax.xml.ws.BindingType;
import javax.xml.ws.soap.SOAPBinding;

@WebService(portName = "TestPort", serviceName = "TestService")
@BindingType(value = SOAPBinding.SOAP12HTTP_BINDING)
public class Soap12Service extends Soap11Service {

}
