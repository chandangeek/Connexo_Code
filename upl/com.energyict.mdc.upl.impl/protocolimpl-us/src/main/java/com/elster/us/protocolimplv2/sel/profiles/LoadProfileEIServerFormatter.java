package com.elster.us.protocolimplv2.sel.profiles;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.elster.us.protocolimplv2.sel.profiles.structure.Interval;
import com.elster.us.protocolimplv2.sel.profiles.structure.LPData;
import com.energyict.mdc.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.LoadProfileReader;

public class LoadProfileEIServerFormatter {
  
  private LDPData data;
  
  public LoadProfileEIServerFormatter(LDPData data) {
    this.data = data;
  }
  
  public List<IntervalData> getIntervalData(List<Integer> channelIndexes) {
    boolean EOIFirstIntvlFlag = true;
    float[] previousEOIValues = new float[]{0,0,0,0,0,0,0,0};
    List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
    for (LPData lpdata : data.getLpData()) {
      for (Interval interval : lpdata.getIntervals()) {
        Date endTimeStamp = getTimeStamp(interval.getYear(),interval.getJulianDay(),interval.getTenthsMillSecSinceMidnight());
        IntervalData intervalData = new IntervalData(endTimeStamp, interval.getStatus());
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
  
  public float getCOI(float previous, float current) {
    return current - previous;
  }
  
  public CollectedLoadProfileConfiguration getConfiguration(LoadProfileReader lpr) {
    //CollectedLoadProfileConfiguration config = new CollectedLoadProfileConfiguration();
    return null;
  }
  
  public int getProtocolStatus() {
    // TODO not sure what this is but may be used for setting protocol specific status
    return 0;
  }

}
