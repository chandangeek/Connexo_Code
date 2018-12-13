package com.elster.us.protocolimplv2.sel.registers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;

import com.elster.us.protocolimplv2.sel.utility.DateFormatHelper;
import static com.elster.us.protocolimplv2.sel.Consts.OBJECT_DIRECTION_DELIVERED;
import static com.elster.us.protocolimplv2.sel.Consts.OBJECT_DIRECTION_RECEIVED;
import static com.elster.us.protocolimplv2.sel.Consts.OBJECT_DIRECTION_IN;
import static com.elster.us.protocolimplv2.sel.Consts.OBJECT_DIRECTION_OUT;
import static com.elster.us.protocolimplv2.sel.Consts.OBJECT_DIRECTION_NA;

public class RegisterParser {
  private byte[] registerData;
  private String strRegisterData;
  private static String groupToken = "\n\r\n";
  private static String registerListToken = "\r\n";
  private static String date = "Date";
  private static String time = "Time";
  
  
  public List<RegisterData> parse(String registerResponse) {
    List<RegisterData> retVal = new ArrayList<RegisterData>();
    RegisterData data = null;
    Date eventDate = null;
    String description = null;
    String[] groups = registerResponse.split(groupToken);
    for(String group : groups) {
      if(group.contains(date)) {
        eventDate = getTimeStamp(group);
      } else if(group.contains("Time Source")) {
        //ignore this
      } else { //register groups A,B,C,P3
        List<Bucket> buckets = new ArrayList<Bucket>();
        String[] regList = group.trim().split(registerListToken);
        int index = 0;
        String bucketHeader = null;
        for(String reg : regList) {
          if(index == 0) {
            bucketHeader = reg;
            index++;
            continue;
          }
          buckets = getBuckets(bucketHeader);
          data = new RegisterData();
          data.setTimestamp(eventDate);
          description = getDescription(reg);
          data.setDescription(description);
          data.setUnit(getUnit(reg));
          data.setDirection(getDirection(description));
          for(int i=0; i < buckets.size(); i++) {
            buckets.get(i).setValue(getRegisterValue(reg, i+1));
          }
          data.setBuckets(buckets);
          retVal.add(data);
        }
      }
      //retVal.add(data);
    }
    return retVal;
  }

  private String getDirection(String description) {
    if(description.contains(OBJECT_DIRECTION_DELIVERED) || description.contains(OBJECT_DIRECTION_IN)) {
      return OBJECT_DIRECTION_DELIVERED;
    } else if (description.contains(OBJECT_DIRECTION_RECEIVED) || description.contains(OBJECT_DIRECTION_OUT)) {
      return OBJECT_DIRECTION_RECEIVED;
    }
    return OBJECT_DIRECTION_NA;
  }

  private String getUnit(String input) {
    String unit = input.substring(input.indexOf('(')+1, input.indexOf(')'));
    return unit.trim();
  }

  private String getDescription(String input) {
    String description = input.substring(0, input.indexOf('('));
    return description.trim();
  }

  private BigDecimal getRegisterValue(String input, int index) {
    BigDecimal retVal = new BigDecimal(-1);
    String[] values = input.trim().split("\\s+");
    int i = 0;
    for(String value : values) {
      if(value.endsWith(")")) {
        //get the index and add the input index to find value
        if(values.length > i+index) {
          return new BigDecimal(values[i+index]);
        }
      }
      i++;
    }
    return retVal;
  }

  private List<Bucket> getBuckets(String input) {
    List<Bucket> buckets = new ArrayList<Bucket>();
    String[] strBuckets = input.trim().split("\\s+");
    int index = 1;
    for(String bucketId : strBuckets) {
      Bucket bucket = new Bucket();
      bucket.setId(bucketId);
      bucket.setSequence(index);
      index++;
      buckets.add(bucket);
    }
    return buckets;
  }

  private Date getTimeStamp(String group) {
    int startDate = group.indexOf(':') +2;
    int endDate = startDate + 8; /* mm/dd/yy  */
    int startTime = group.indexOf(':', startDate) +2;
    int endTime = startTime + 8; /* hh:mm:ss */
    String dateStr = group.substring(startDate, endDate);
    String timeStr = group.substring(startTime, endTime);
    
    Date d = DateFormatHelper.getDate(dateStr, "MM/dd/yy");
    Calendar cal = Calendar.getInstance();
    cal.setTime(d);
    DateFormatHelper.setTimeIntoDate(cal, timeStr);

    // TODO: get the timezone from properties
    TimeZone tz = TimeZone.getTimeZone("US/Pacific");
    cal.setTimeZone(tz);

    return cal.getTime();
  }

}
