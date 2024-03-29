/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.service.soap.runtime.attachments;

import static org.mule.runtime.soap.api.SoapVersion.SOAP11;

import org.mule.service.soap.service.Mtom11Service;
import org.mule.service.soap.service.Mtom12Service;

public class MtomAttachmentsTestCase extends AttachmentsTestCase {

  @Override
  protected String getServiceClass() {
    return soapVersion.equals(SOAP11) ? Mtom11Service.class.getName() : Mtom12Service.class.getName();
  }

  @Override
  protected boolean isMtom() {
    return true;
  }
}
