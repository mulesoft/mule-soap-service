/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.service;

import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;

@MTOM
@WebService(portName = "TestPort", serviceName = "TestService")
public class Mtom11Service extends Soap11Service {

}
