/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.service;

import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;

@MTOM
@WebService(portName = "TestPort", serviceName = "TestService")
public class Mtom11Service extends Soap11Service {

}
