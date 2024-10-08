/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.service;

import jakarta.jws.WebService;
import jakarta.xml.ws.BindingType;
import jakarta.xml.ws.soap.MTOM;
import jakarta.xml.ws.soap.SOAPBinding;

@MTOM
@WebService(portName = "TestPort", serviceName = "TestService")
@BindingType(value = SOAPBinding.SOAP12HTTP_BINDING)
public class Mtom12Service extends Soap12Service {

}
