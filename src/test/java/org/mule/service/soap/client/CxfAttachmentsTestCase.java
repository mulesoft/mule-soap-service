/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.client;

import static java.util.Collections.emptyMap;
import static java.util.Collections.indexOfSubList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_DISPOSITION;

import java.io.ByteArrayInputStream;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import org.apache.cxf.message.Attachment;
import org.apache.tika.io.NullInputStream;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.extension.api.soap.SoapAttachment;

public class CxfAttachmentsTestCase {

  @Test
  public void notMtom() {
    CxfAttachmentsFactory factory = new CxfAttachmentsFactory(false);
    assertThat(factory.transformToCxfAttachments(buildAttachments()), is(emptyMap()));
  }

  @Test
  public void MtomEnabled() {
    CxfAttachmentsFactory factory = new CxfAttachmentsFactory(true);
    Map<String, Attachment> attachments = factory.transformToCxfAttachments(buildAttachments());
    assertThat(attachments.keySet(), hasSize(3));
    assertThat(attachments.keySet(),
               containsInAnyOrder(new AttachmentMatcher(attachments, "name1", MediaType.APPLICATION_JAVA),
                                  new AttachmentMatcher(attachments, "name2", MediaType.APPLICATION_XML),
                                  new AttachmentMatcher(attachments, "name3", MediaType.APPLICATION_JSON)));
  }

  private Map<String, SoapAttachment> buildAttachments() {
    ImmutableMap.Builder builder = new ImmutableMap.Builder();
    builder.put("name1", new SoapAttachment(new NullInputStream(10), MediaType.APPLICATION_JAVA));
    builder.put("name2", new SoapAttachment(new NullInputStream(10), MediaType.APPLICATION_XML));
    builder.put("name3", new SoapAttachment(new NullInputStream(10), MediaType.APPLICATION_JSON));
    return builder.build();
  }

  private class AttachmentMatcher extends BaseMatcher<String> {

    private String expectedName;
    private String expectedMediaType;
    private Map<String, Attachment> attachmentMap;

    public AttachmentMatcher(Map<String, Attachment> map, String expectedName, MediaType expectedMediaType) {
      this.expectedName = expectedName;
      this.expectedMediaType = expectedMediaType.toRfcString();
      this.attachmentMap = map;
    }

    @Override
    public boolean matches(Object o) {
      String name = (String) o;
      if (!this.expectedName.equals(name)) {
        return false;
      }

      String actualMediaType = attachmentMap.get(name).getDataHandler().getContentType();
      if (!expectedMediaType.equals(actualMediaType)) {
        return false;
      }

      return attachmentMap.get(name).getHeader(CONTENT_DISPOSITION).contains("attachment");
    }

    @Override
    public void describeTo(Description description) {}
  }
}
