package com.elster.us.protocolimplv2.sel.profiles.structure;

import java.util.List;

public class MeterConfiguration {
  public int currYear;
  public int currJulianDay;
  public long currTenthsMillSecSinceMidnight;
  public int timeSource;
  public String startDSTConfigString;
  public String stopDSTConfigString;
  public int dstForwardTime; //minutes after midnight
  public int dstBackwardTime; //minutes after midnight
  public float ctr;
  public float ctrn;
  public float ptr;
  public String fid; //firmware version
  public String mid;
  public String tid;
  public int meterForm;
  public int serRecordsAvailable;
  public int firstLDPRecordYear;
  public int firstLDPRecordJulianDay;
  public long firstLDPRecordTenthsMillSecSinceMidnight;
  public int lastLDPRecordYear;
  public int lastLDPRecordJulianDay;
  public long lastLDPRecordTenthsMillSecSinceMidnight;
  public long numberLDPRecordsAvailable;
  public int ldarSetting;
  public int numberLDPChannelsEnabled;
  public List<String> channelNames;
  public List<String> recorderNames;
  
  
  public int getCurrYear() {
    return currYear;
  }
  public void setCurrYear(int currYear) {
    this.currYear = currYear;
  }
  public int getCurrJulianDay() {
    return currJulianDay;
  }
  public void setCurrJulianDay(int currJulianDay) {
    this.currJulianDay = currJulianDay;
  }
  public long getCurrTenthsMillSecSinceMidnight() {
    return currTenthsMillSecSinceMidnight;
  }
  public void setCurrTenthsMillSecSinceMidnight(long currTenthsMillSecSinceMidnight) {
    this.currTenthsMillSecSinceMidnight = currTenthsMillSecSinceMidnight;
  }
  public int getTimeSource() {
    return timeSource;
  }
  public void setTimeSource(int timeSource) {
    this.timeSource = timeSource;
  }
  public String getStartDSTConfigString() {
    return startDSTConfigString;
  }
  public void setStartDSTConfigString(String startDSTConfigString) {
    this.startDSTConfigString = startDSTConfigString;
  }
  public String getStopDSTConfigString() {
    return stopDSTConfigString;
  }
  public void setStopDSTConfigString(String stopDSTConfigString) {
    this.stopDSTConfigString = stopDSTConfigString;
  }
  public int getDstForwardTime() {
    return dstForwardTime;
  }
  public void setDstForwardTime(int dstForwardTime) {
    this.dstForwardTime = dstForwardTime;
  }
  public int getDstBackwardTime() {
    return dstBackwardTime;
  }
  public void setDstBackwardTime(int dstBackwardTime) {
    this.dstBackwardTime = dstBackwardTime;
  }
  public float getCtr() {
    return ctr;
  }
  public void setCtr(float ctr) {
    this.ctr = ctr;
  }
  public float getCtrn() {
    return ctrn;
  }
  public void setCtrn(float ctrn) {
    this.ctrn = ctrn;
  }
  public float getPtr() {
    return ptr;
  }
  public void setPtr(float ptr) {
    this.ptr = ptr;
  }
  public String getFid() {
    return fid;
  }
  public void setFid(String fid) {
    this.fid = fid;
  }
  public String getMid() {
    return mid;
  }
  public void setMid(String mid) {
    this.mid = mid;
  }
  public String getTid() {
    return tid;
  }
  public void setTid(String tid) {
    this.tid = tid;
  }
  public int getMeterForm() {
    return meterForm;
  }
  public void setMeterForm(int meterForm) {
    this.meterForm = meterForm;
  }
  public int getSerRecordsAvailable() {
    return serRecordsAvailable;
  }
  public void setSerRecordsAvailable(int serRecordsAvailable) {
    this.serRecordsAvailable = serRecordsAvailable;
  }
  public int getFirstLDPRecordYear() {
    return firstLDPRecordYear;
  }
  public void setFirstLDPRecordYear(int firstLDPRecordYear) {
    this.firstLDPRecordYear = firstLDPRecordYear;
  }
  public int getFirstLDPRecordJulianDay() {
    return firstLDPRecordJulianDay;
  }
  public void setFirstLDPRecordJulianDay(int firstLDPRecordJulianDay) {
    this.firstLDPRecordJulianDay = firstLDPRecordJulianDay;
  }
  public long getFirstLDPRecordTenthsMillSecSinceMidnight() {
    return firstLDPRecordTenthsMillSecSinceMidnight;
  }
  public void setFirstLDPRecordTenthsMillSecSinceMidnight(long firstLDPRecordTenthsMillSecSinceMidnight) {
    this.firstLDPRecordTenthsMillSecSinceMidnight = firstLDPRecordTenthsMillSecSinceMidnight;
  }
  public int getLastLDPRecordYear() {
    return lastLDPRecordYear;
  }
  public void setLastLDPRecordYear(int lastLDPRecordYear) {
    this.lastLDPRecordYear = lastLDPRecordYear;
  }
  public int getLastLDPRecordJulianDay() {
    return lastLDPRecordJulianDay;
  }
  public void setLastLDPRecordJulianDay(int lastLDPRecordJulianDay) {
    this.lastLDPRecordJulianDay = lastLDPRecordJulianDay;
  }
  public long getLastLDPRecordTenthsMillSecSinceMidnight() {
    return lastLDPRecordTenthsMillSecSinceMidnight;
  }
  public void setLastLDPRecordTenthsMillSecSinceMidnight(long lastLDPRecordTenthsMillSecSinceMidnight) {
    this.lastLDPRecordTenthsMillSecSinceMidnight = lastLDPRecordTenthsMillSecSinceMidnight;
  }
  public long getNumberLDPRecordsAvailable() {
    return numberLDPRecordsAvailable;
  }
  public void setNumberLDPRecordsAvailable(long numberLDPRecordsAvailable) {
    this.numberLDPRecordsAvailable = numberLDPRecordsAvailable;
  }
  public int getLdarSetting() {
    return ldarSetting;
  }
  public void setLdarSetting(int ldarSetting) {
    this.ldarSetting = ldarSetting;
  }
  public int getNumberLDPChannelsEnabled() {
    return numberLDPChannelsEnabled;
  }
  public void setNumberLDPChannelsEnabled(int numberLDPChannelsEnabled) {
    this.numberLDPChannelsEnabled = numberLDPChannelsEnabled;
  }
  public List<String> getChannelNames() {
    return channelNames;
  }
  public void setChannelNames(List<String> channelNames) {
    this.channelNames = channelNames;
  }
  public List<String> getRecorderNames() {
    return recorderNames;
  }
  public void setRecorderNames(List<String> recorderNames) {
    this.recorderNames = recorderNames;
  }
  
  

}
