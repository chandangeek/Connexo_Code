package com.elster.us.protocolimplv2.sel.profiles.structure;

public class Event {
  private int year;
  private int julianDay;
  private long tenthsMillSecSinceMidnight;
  private int serData;
  private int meterWordBit;
  private boolean asserted;

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
  public int getSerData() {
    return serData;
  }
  public void setSerData(int serData) {
    this.serData = serData;
    parseSerData(serData);
  }
  private void parseSerData(int serEventId) {
    String hex = Integer.toHexString(serEventId);
    char assertionBit = hex.charAt(0);
    if(assertionBit == '1') {
      setAsserted(false);
    } else {
      setAsserted(true);
    }
    String deviceWordBit = hex.substring(1);
    this.setMeterWordBit(Integer.parseInt(deviceWordBit, 16));
  }
  public int getMeterWordBit() {
    return meterWordBit;
  }
  public void setMeterWordBit(int meterWordBit) {
    this.meterWordBit = meterWordBit;
  }
  public boolean isAsserted() {
    return asserted;
  }
  public void setAsserted(boolean asserted) {
    this.asserted = asserted;
  }
}
