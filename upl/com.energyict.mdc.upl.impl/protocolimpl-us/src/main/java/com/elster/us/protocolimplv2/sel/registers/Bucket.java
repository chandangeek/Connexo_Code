package com.elster.us.protocolimplv2.sel.registers;

import java.math.BigDecimal;

public class Bucket {
  private String id;
  private BigDecimal value;
  private int sequence;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public BigDecimal getValue() {
    return value;
  }
  public void setValue(BigDecimal value) {
    this.value = value;
  }
  public int getSequence() {
    return sequence;
  }
  public void setSequence(int sequence) {
    this.sequence = sequence;
  }
}
