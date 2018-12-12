package com.elster.us.protocolimplv2.sel.profiles.structure;

import java.awt.*;
import java.util.List;

public class SERData {
  private List<Event> events;
  private int checkSum;
  
  public List<Event> getEvents() {
    return events;
  }
  public void setEvents(List<Event> events) {
    this.events = events;
  }
  public int getCheckSum() {
    return checkSum;
  }
  public void setCheckSum(int checkSum) {
    this.checkSum = checkSum;
  }
}
