package com.elster.us.protocolimplv2.sel.utility;

import static com.elster.us.protocolimplv2.sel.Consts.OBJECT_KVARH_DELIVERED;
import static com.elster.us.protocolimplv2.sel.Consts.OBJECT_KWH_DELIVERED;

import java.util.HashMap;
import java.util.Map;

import com.elster.us.protocolimplv2.sel.Consts;
import com.energyict.obis.ObisCode;

public class EventMapper {
  public final static int HALARM = 80;
  public final static int SALARM = 72;
  public final static int RSTDEM = 43;
  public final static int RSTENGY = 90;
  public final static int RSTPKDM = 41;
  public final static int TEST = 52;
  public final static int DSTCH = 62;
  public final static int SSI_EVE = 50;
  public final static int FAULT = 54;
  public final static int HARM02 = 6;
  public final static int HARM03 = 5;
  public final static int HARM04 = 4;
  public final static int HARM05 = 3;
  public final static int HARM06 = 2;
  public final static int HARM07 = 1;
  public final static int HARM08 = 0;
  public final static int HARM09 = 15;
  public final static int HARM10 = 14;
  public final static int HARM11 = 13;
  public final static int HARM12 = 12;
  public final static int HARM13 = 11;
  public final static int HARM14 = 10;
  public final static int HARM15 = 9;
  public final static String EVENT_UNDEFINED = "UNDEFINED_EVENT";

  private static Map<Integer, String> map = new HashMap<Integer, String>();

  static {
      // event mappings
      map.put(HALARM, Consts.EVENT_HALARM);
      map.put(SALARM, Consts.EVENT_SALARM);
      map.put(RSTDEM, Consts.EVENT_RSTDEM);
      map.put(RSTENGY, Consts.EVENT_RSTENGY);
      map.put(RSTPKDM, Consts.EVENT_RSTPKDM);
      map.put(TEST, Consts.EVENT_TEST);
      map.put(DSTCH, Consts.EVENT_DSTCH);
      map.put(SSI_EVE, Consts.EVENT_SSI_EVE);
      map.put(FAULT, Consts.EVENT_FAULT);
      map.put(HARM02, Consts.EVENT_HARM02);
      map.put(HARM03, Consts.EVENT_HARM03);
      map.put(HARM04, Consts.EVENT_HARM04);
      map.put(HARM05, Consts.EVENT_HARM05);
      map.put(HARM06, Consts.EVENT_HARM06);
      map.put(HARM07, Consts.EVENT_HARM07);
      map.put(HARM08, Consts.EVENT_HARM08);
      map.put(HARM09, Consts.EVENT_HARM09);
      map.put(HARM10, Consts.EVENT_HARM10);
      map.put(HARM11, Consts.EVENT_HARM11);
      map.put(HARM12, Consts.EVENT_HARM12);
      map.put(HARM13, Consts.EVENT_HARM13);
      map.put(HARM14, Consts.EVENT_HARM14);
      map.put(HARM15, Consts.EVENT_HARM15);
  }

  // Prevent instantiation
  private EventMapper() {}
  
  public static String mapEventId(int eventId) {
    String event = map.get(eventId);
    return event != null ? event : EVENT_UNDEFINED;
}

}
