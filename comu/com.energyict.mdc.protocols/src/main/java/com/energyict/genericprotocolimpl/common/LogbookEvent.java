/*
 * LogbookEvent.java
 *
 * Created on 5 december 2007, 15:51
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.energyict.protocol.MeterEvent;

/**
 *
 * @author kvds
 */
public class LogbookEvent { 
    
    static List list=new ArrayList();
    static {
        list.add(new LogbookEvent(1,"Parameter changed","Indicates that one or several parameters have been changed.",MeterEvent.CONFIGURATIONCHANGE));
        list.add(new LogbookEvent(4,"Event log cleared","Indicates that the logbook was cleared. ",MeterEvent.CLEAR_DATA));
        list.add(new LogbookEvent(9,"Daylight saving time enabled or disabled","Indicates the regular change from and to daylight saving time. The time stamp shows the time before the change.",MeterEvent.OTHER));
        list.add(new LogbookEvent(10,"Clock adjusted (old date/time)","Indicates that the clock has been adjusted. The date/time that is stored in the event log is the old date/time before adjusting the clock.",MeterEvent.SETCLOCK_BEFORE));
        list.add(new LogbookEvent(11,"Clock adjusted (new date/time)","Indicates that the clock has been adjusted. The date/time that is stored in the event log is the new date/time after adjusting the clock.",MeterEvent.SETCLOCK_AFTER));
        list.add(new LogbookEvent(17,"Sag (Under Voltage)","Indicates that the voltage dropped below the under voltage threshold.",MeterEvent.VOLTAGE_SAG));
        list.add(new LogbookEvent(18,"reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(19,"reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(20,"Swell (Over Voltage) ","",MeterEvent.VOLTAGE_SWELL));
        list.add(new LogbookEvent(21,"reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(22,"reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(23,"Power down","Indicates that a total power failure occurred.",MeterEvent.POWERDOWN));
        list.add(new LogbookEvent(24,"Power up after short power down","Indicates that the power returned before the long power failure threshold has been reached.",MeterEvent.POWERUP));
        list.add(new LogbookEvent(45,"Error register cleared ","Indicates that the error register was cleared.",MeterEvent.OTHER));
        list.add(new LogbookEvent(49,"reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(50,"reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(51,"reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(66,"reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(75,"reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(76,"reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(77,"Flash memory access error","Indicates an access error to the flash memory.",MeterEvent.ROM_MEMORY_ERROR));
        list.add(new LogbookEvent(79,"reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(81,"Program memory checksum error","Indicates a checksum error in the program memory.",MeterEvent.ROM_MEMORY_ERROR));
        list.add(new LogbookEvent(82,"Backup data checksum error","Indicates a checksum error in the backup data.",MeterEvent.ROM_MEMORY_ERROR));
        list.add(new LogbookEvent(83,"Parameter checksum error","Indicates a checksum error in the parameter data.",MeterEvent.RAM_MEMORY_ERROR));
        list.add(new LogbookEvent(84,"Profile data checksum error","Indicates a checksum error in the profile data (energy values profile, billing values profile, event log). ",MeterEvent.RAM_MEMORY_ERROR));
        list.add(new LogbookEvent(89,"Invalid start up ","watch dog reset.",MeterEvent.WATCHDOGRESET));
        list.add(new LogbookEvent(90,"reserved ","",MeterEvent.OTHER));
        list.add(new LogbookEvent(91,"Association error","Indicates that an error occurred during the establishment of the application association.",MeterEvent.OTHER));
        list.add(new LogbookEvent(133,"Terminal cover removed","Indicates that the terminal cover has been removed. ",MeterEvent.METER_ALARM));
        list.add(new LogbookEvent(158,"Billing values profile cleared","Indicates that the daily energy value profile was cleared.",MeterEvent.BILLING_ACTION));
        list.add(new LogbookEvent(159,"Load profile energy cleared","Indicates that the load profile was cleared",MeterEvent.CLEAR_DATA));
        list.add(new LogbookEvent(160,"Power up after long power down","Indicates that the power returned after the long power failure threshold has been reached.",MeterEvent.POWERUP));
        list.add(new LogbookEvent(161,"reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(162,"TOU activated","Indicates that the passive TOU has been activated.",MeterEvent.OTHER));
        list.add(new LogbookEvent(163,"reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(164,"reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(165,"Reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(166,"reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(167,"reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(168,"Breaker set to ready to connect","Indicates that the Breaker is remotely set to Ready to connect",MeterEvent.OTHER));
        list.add(new LogbookEvent(170,"Breaker remote connected","Indicates that the breaker has been remotely set to connect",MeterEvent.OTHER));
        list.add(new LogbookEvent(171,"Breaker remote disconnect","Indicates that the breaker has been remotely disconnected.",MeterEvent.OTHER));
        list.add(new LogbookEvent(173,"Breaker manually connected","Indicates that the breaker has been manually set from ready to connect state to connected state.",MeterEvent.OTHER));
        list.add(new LogbookEvent(174,"Breaker manually disconnected","Indicates that the breaker has been manually disconnected.",MeterEvent.OTHER));
        list.add(new LogbookEvent(175,"Maximum demand exceeded","Indicates that the maximum demand threshold has been exceeded. ",MeterEvent.OTHER));
        list.add(new LogbookEvent(176,"reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(177,"Reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(178,"Reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(179,"End of the Sag No under voltage anymore","Indicates that the meter voltage returned to nominal voltage after under voltage.Linked to event 17.",MeterEvent.OTHER));
        list.add(new LogbookEvent(180,"Reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(181,"Reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(182,"End of the swell. No over voltage  anymore","Indicates that the meter voltage  returned to nominal voltage after over voltage.Linked to event 20.",MeterEvent.OTHER));
        list.add(new LogbookEvent(183,"Reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(184,"Reserved","",MeterEvent.OTHER));
        list.add(new LogbookEvent(186,"Maximum Demand OK","Indicates that the demand dropped below the maximum demand threshold.",MeterEvent.OTHER));
        list.add(new LogbookEvent(187,"Terminal cover closed","Indicates that the terminal cover has been closed. Linked to event 133. ",MeterEvent.OTHER));
        list.add(new LogbookEvent(189,"reserved","Indicates that the breaker log was cleared.",MeterEvent.OTHER));
    }
    
    private int id;
    private String event;
    private String eventDescription;
    private int meterEventCode;
    
    /** Creates a new instance of LogbookEvent */
    private LogbookEvent(int id,String event,String eventDescription,int meterEventCode) {
        this.id=id;
        this.event=event;
        this.eventDescription=eventDescription;
        this.meterEventCode=meterEventCode;
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LogbookEvent:\n");
        strBuff.append("   event="+getEvent()+"\n");
        strBuff.append("   eventDescription="+getEventDescription()+"\n");
        strBuff.append("   id="+getId()+"\n");
        strBuff.append("   meterEventCode="+getMeterEventCode()+"\n");
        return strBuff.toString();
    }
    
    static public LogbookEvent findLogbookEvent(int id) {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            LogbookEvent o = (LogbookEvent)it.next();
            if (o.getId()==id){
            	return o;
            }
        }
        return new LogbookEvent(id,"Unknown LogbookEvent id "+id,"",MeterEvent.OTHER);        
    }

    public MeterEvent meterEvent(Date date) {
        return new MeterEvent(date,getMeterEventCode(),getId(), getEvent()+(getEventDescription().compareTo("")==0?"":", "+getEventDescription()));
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public int getMeterEventCode() {
        return meterEventCode;
    }

    public void setMeterEventCode(int meterEventCode) {
        this.meterEventCode = meterEventCode;
    }
    
}
