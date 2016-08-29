package com.elster.us.protocolimplv2.sel.profiles.structure;

public class Interval {
  private int status;
  private int year;
  private int julianDay;
  private long tenthsMillSecSinceMidnight;
  private float[] channelValues;
  
  
  public int getStatus() {
    return status;
  }
  public void setStatus(int status) {
    this.status = status;
  }
  public int getYear() {
    return year;
  }
  public void setYear(int year) {
    this.year = year;
  }
  public int getJulianDay() {
    return julianDay;
  }
  public void setJulianDay(int julianDay) {
    this.julianDay = julianDay;
  }
  public long getTenthsMillSecSinceMidnight() {
    return tenthsMillSecSinceMidnight;
  }
  public void setTenthsMillSecSinceMidnight(long tenthsMillSecSinceMidnight) {
    this.tenthsMillSecSinceMidnight = tenthsMillSecSinceMidnight;
  }
  public float[] getChannelValues() {
    return channelValues;
  }
  public void setChannelValues(float[] channelValues) {
    this.channelValues = channelValues;
  }

}
