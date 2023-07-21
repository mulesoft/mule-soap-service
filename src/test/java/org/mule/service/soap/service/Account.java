/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.service.soap.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Account {

  private Long id;
  private String clientName;
  private Calendar startingDate;
  private List<String> items = new ArrayList<>();

  public Account() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getClientName() {
    return clientName;
  }

  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  public Calendar getStartingDate() {
    return startingDate;
  }

  public void setStartingDate(Calendar startingDate) {
    this.startingDate = startingDate;
  }

  public List<String> getItems() {
    return items;
  }

  public void setItems(List<String> items) {
    this.items = items;
  }
}
