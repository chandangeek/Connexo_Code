package com.elster.us.protocolimplv2.sel.utility;

import static com.elster.us.protocolimplv2.sel.Consts.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateFormatHelper {
  private static Map<String, SimpleDateFormat> formatters = new HashMap<String,SimpleDateFormat>();

  private static SimpleDateFormat DEFAULT = new SimpleDateFormat(MMDDYY);

  private DateFormatHelper() {}

  static {
      formatters.put("0", new SimpleDateFormat(MMDDYY));
      formatters.put("1", new SimpleDateFormat(DDMMYY));
      formatters.put("2", new SimpleDateFormat(YYMMDD));
  }

  public static SimpleDateFormat get(String key) {
      if (formatters.containsKey(key)) {
          return formatters.get(key);
      }
      // TODO: logging
      return DEFAULT;
  }

  public static Date getDate(String dateStr, String dateFormatStr) {
      try {
          return get(dateFormatStr).parse(dateStr);
      } catch (ParseException pe) {
          // Failed to parse the date
          // TODO: logging
          System.out.println("Twit");
      }
      return new Date();
  }

  public static void setTimeIntoDate(Calendar d, String timeStr) {
      StringTokenizer st = new StringTokenizer(timeStr, ":");
      int count = 0;
      while (st.hasMoreTokens()) {
          String token = st.nextToken();
          switch (count) {
              case 0:
                  // TODO: think about whether this is right? DOes it come back 24h/12h etc?
                  d.set(Calendar.HOUR_OF_DAY, Integer.parseInt(token));
                  break;
              case 1:
                  d.set(Calendar.MINUTE, Integer.parseInt(token));
                  break;
              case 2:
                  d.set(Calendar.SECOND, Integer.parseInt(token));
                  break;
          }
          count++;
      }
  }

}
