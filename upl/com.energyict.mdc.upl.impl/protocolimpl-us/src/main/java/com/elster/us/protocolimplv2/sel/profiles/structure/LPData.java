package com.elster.us.protocolimplv2.sel.profiles.structure;

import java.util.List;

public class LPData {
  private List<Interval> intervals;
  private int checkSum;
  private int calCheckSum;
  
  public List<Interval> getIntervals() {
    return intervals;
  }
  public void setIntervals(List<Interval> intervals) {
    this.intervals = intervals;
  }
  public int getCheckSum() {
    return checkSum;
  }
  public void setCheckSum(int checkSum) {
    this.checkSum = checkSum;
  }
  public int getCalCheckSum() {
    return calCheckSum;
  }
  public void setCalCheckSum(int calCheckSum) {
    this.calCheckSum = calCheckSum;
  }
}
