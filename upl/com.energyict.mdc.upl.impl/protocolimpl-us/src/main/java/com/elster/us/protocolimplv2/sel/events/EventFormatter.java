package com.elster.us.protocolimplv2.sel.events;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.elster.us.protocolimplv2.sel.SELProperties;
import com.elster.us.protocolimplv2.sel.profiles.structure.Event;
import com.elster.us.protocolimplv2.sel.profiles.structure.SERData;
import com.elster.us.protocolimplv2.sel.utility.DateFormatHelper;
import com.elster.us.protocolimplv2.sel.utility.EventMapper;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterEvent;

public class EventFormatter {
  
  private List<SERData> data;
  private SELProperties properties;
  
  public EventFormatter(List<SERData> data, SELProperties properties) {
    this.data = data;
    this.properties = properties;
  }
  
  public void addAllEventsFromSER(LogBookReader logBook, CollectedLogBook deviceLogBook) {
    List<MeterEvent> meterEvents = new ArrayList<>();
    for (SERData serdata : data) {
      for(Event event : serdata.getEvents()) {
        MeterEvent meterEvent = createEvent(event);
        if (meterEvent.getTime().after(logBook.getLastLogBook())) {
            meterEvents.add(meterEvent);
        }
      } 
    }
    deviceLogBook.addCollectedMeterEvents(MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents));
}
  
  private MeterEvent createEvent(Event event) {
    Date eventDate = getTimeStamp(event.getYear(),event.getJulianDay(),event.getTenthsMillSecSinceMidnight());
    Date eventDateMeterTz = DateFormatHelper.convertTimeZone(eventDate, properties.getDeviceTimezone(), properties.getTimezone());
    MeterEvent meterEvent = new MeterEvent(
        eventDateMeterTz, 
        mapEventToEICode(event.getMeterWordBit()), 
        event.getMeterWordBit(), 
        EventMapper.mapEventId(event.getMeterWordBit()) + " - " + (event.isAsserted() ? "Asserted" : "Deasserted"), 
        0, 
        0
    );
//    MeterProtocolEvent protocolEvent;
//    protocolEvent = new MeterProtocolEvent(
//        eventDateMeterTz,
//        mapEventToEICode(serData),
//        serData,
//        EndDeviceEventTypeFactory.getConfigurationChangeEventType(),
//        EventMapper.mapEventId(serData),
//        0,
//        0
//    );
    
    return meterEvent;
  }

  private Date getTimeStamp(int year, int julianDay, long tenthsMillisSinceMidnight) {
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
  
  
  private int mapEventToEICode(int event) {
    if(event >= 0 && event <= 15) // Harmonic Threshold Events
      return MeterEvent.LIMITER_THRESHOLD_EXCEEDED;
    switch (event) {
      case EventMapper.HALARM:
        return MeterEvent.METER_ALARM;
      case EventMapper.SALARM:
        return MeterEvent.METER_ALARM;
      case EventMapper.RSTDEM:
        return MeterEvent.MAXIMUM_DEMAND_RESET;
      case EventMapper.RSTENGY:
        return MeterEvent.ALARM_REGISTER_CLEARED;
      case EventMapper.RSTPKDM:
        return MeterEvent.MAXIMUM_DEMAND_RESET;
      case EventMapper.TEST:
        return MeterEvent.OTHER;
      case EventMapper.DSTCH:
        return MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED;
      case EventMapper.SSI_EVE:
        return MeterEvent.VOLTAGE_SAG;
      case EventMapper.FAULT:
        return MeterEvent.OTHER;
      default:
        return MeterEvent.OTHER;
    }
  }

}
