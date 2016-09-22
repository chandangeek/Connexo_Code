package com.elster.us.protocolimplv2.sel.profiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.elster.us.protocolimplv2.sel.SELProperties;
import com.elster.us.protocolimplv2.sel.profiles.structure.Interval;
import com.elster.us.protocolimplv2.sel.profiles.structure.LPData;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;

public class LoadProfileEIServerFormatter {
  
  private LDPData data;
  private SELProperties properties;
  
  public LoadProfileEIServerFormatter(LDPData data, SELProperties properties) {
    this.data = data;
    this.properties = properties;
  }
  
  public List<IntervalData> getIntervalData(List<Integer> channelIndexes) {
    boolean EOIFirstIntvlFlag = true;
    Number[] previousEOIValues = new Number[]{0,0,0,0,0,0,0,0};
    List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
    for (LPData lpdata : data.getLpData()) {
      for (Interval interval : lpdata.getIntervals()) {
        Date endTimeStamp = getTimeStamp(interval.getYear(),interval.getJulianDay(),interval.getTenthsMillSecSinceMidnight());
        Date endTimeMeterTz = adjustTimeUsingMeterTz(endTimeStamp, properties.getDeviceTimezone());
        IntervalData intervalData = new IntervalData(endTimeMeterTz);
        addMeterStatuses(intervalData, interval.getStatus());
        for(int i : channelIndexes) {
          if(data.getMeterConfig().getRecorderNames().get(0).equalsIgnoreCase("EOI")) {
            intervalData.addValue(getCOI(previousEOIValues[i], interval.getChannelValues()[i]));
            previousEOIValues[i] = interval.getChannelValues()[i];
          } else {
            intervalData.addValue(interval.getChannelValues()[i]);
          }
        }
        if(!EOIFirstIntvlFlag) {
          intervalDatas.add(intervalData);
        }
        EOIFirstIntvlFlag = false;
      }
    }
    
    return intervalDatas;
  }
  
  private Date adjustTimeUsingMeterTz(Date endTimeStamp, String deviceTimezone) {
    if(endTimeStamp == null)
      return null;
    Calendar cal = Calendar.getInstance();
    cal.setTime(endTimeStamp);
    cal.setTimeZone(TimeZone.getTimeZone(deviceTimezone));
    return cal.getTime();
  }

  public Date getTimeStamp(int year, int julianDay, long tenthsMillisSinceMidnight) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.DAY_OF_YEAR, julianDay);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    
    int milliseconds = (int) (tenthsMillisSinceMidnight / 10);
    cal.add(Calendar.MILLISECOND, milliseconds);

    return cal.getTime();
    
  }
  
  public BigDecimal getCOI(Number previous, Number current) {
    BigDecimal curr = new BigDecimal(current.longValue());
    BigDecimal prev = new BigDecimal(previous.longValue());
    return curr.subtract(prev);
  }
  
  private void addMeterStatuses(IntervalData intervalData, int status) {
    if(status == 0) {
      intervalData.addEiStatus(IntervalStateBits.OK);
      return;
    }
    String seq = Integer.toBinaryString(status);
    String reverseSeq = new StringBuilder(seq).reverse().toString();
    char[] bits = reverseSeq.toCharArray();
    for (int i = 0; i < bits.length; i++) {
      char b=bits[i];
      if(b == '1') {
        switch (i) {
        case 0:
          intervalData.addEiStatus(IntervalStateBits.OTHER);
          break;
        case 1:
          intervalData.addEiStatus(IntervalStateBits.POWERDOWN);
          break;
        case 2:
          intervalData.addEiStatus(IntervalStateBits.BADTIME);
          break;
        case 3:
          intervalData.addEiStatus(IntervalStateBits.BADTIME);
          break;
        case 4:
          intervalData.addEiStatus(IntervalStateBits.CORRUPTED);
          break;
        case 5:
          intervalData.addEiStatus(IntervalStateBits.TEST);
          break;
        case 6:
          intervalData.addEiStatus(IntervalStateBits.OTHER);
          break;
        default:
          intervalData.addEiStatus(IntervalStateBits.OTHER);
        }
      }
      System.out.println(b);
    }
  }
}
