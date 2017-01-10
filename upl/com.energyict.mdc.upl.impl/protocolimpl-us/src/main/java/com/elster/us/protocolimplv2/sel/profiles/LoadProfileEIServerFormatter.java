package com.elster.us.protocolimplv2.sel.profiles;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.elster.us.protocolimplv2.sel.SELProperties;
import com.elster.us.protocolimplv2.sel.profiles.structure.Interval;
import com.elster.us.protocolimplv2.sel.profiles.structure.LPData;
import com.elster.us.protocolimplv2.sel.utility.DateFormatHelper;
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
    List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
    for (LPData lpdata : data.getLpData()) {
      for (Interval interval : lpdata.getIntervals()) {
        Date endTimeStamp = getTimeStamp(interval.getYear(),interval.getJulianDay(),interval.getTenthsMillSecSinceMidnight());
        //Date endTimeMeterTz = DateFormatHelper.convertTimeZone(endTimeStamp, properties.getDeviceTimezone(), properties.getTimezone());
        IntervalData intervalData = new IntervalData(endTimeStamp);
        addMeterStatuses(intervalData, interval.getStatus());
        for(int i : channelIndexes) {
          //don't send zero values returned by meter if interval has power down status flag
          if(isPowerDown(interval.getStatus()) && (interval.getChannelValues()[i].intValue() == 0)) {
            continue;
          }
          else {
            intervalData.addValue(interval.getChannelValues()[i]);
          }
        }
        intervalDatas.add(intervalData);
      }
    }

    return intervalDatas;
  }

  public Date getTimeStamp(int year, int julianDay, long tenthsMillisSinceMidnight) {
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(properties.getDeviceTimezone()));
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

  private boolean isPowerDown(int status) {
    if(status == 0) {
      return false;
    }
    String seq = Integer.toBinaryString(status);
    String reverseSeq = new StringBuilder(seq).reverse().toString();
    char[] bits = reverseSeq.toCharArray();
    for (int i = 0; i < bits.length; i++) {
      char b=bits[i];
      if(b == '1') {
        if(i==1) {
          return true;
        }
      }
    }
    return false;
  }

}
