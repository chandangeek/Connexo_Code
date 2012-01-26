/*
 * CosemActivityCalendarBuilder.java
 *
 * Created on 7 december 2007, 14:40
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;


import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.protocolimpl.edf.messages.objects.*;

import java.util.Iterator;
import java.util.List;
/**
 *
 * @author kvds
 */
public class CosemActivityCalendarBuilder {
    
    ActivityCalendar messageActivityCalendar;
            
    /** Creates a new instance of CosemActivityCalendarBuilder */
    public CosemActivityCalendarBuilder(ActivityCalendar messageActivityCalendar) {
        this.messageActivityCalendar=messageActivityCalendar;
    }
    
    public OctetString calendarNameActive() {
        return new OctetString(new byte[]{messageActivityCalendar.getActiveCalendarName()}); 
    }
    public Array seasonProfileActive() {
        Array array = new Array();
        List seasonProfiles = messageActivityCalendar.getActiveSeasonProfiles();
        Iterator it = seasonProfiles.iterator();
        while(it.hasNext()) {
            Structure structure = new Structure();
            SeasonProfile sp = (SeasonProfile)it.next();
            structure.addDataType(new OctetString(new byte[]{sp.getName()}));
            structure.addDataType(new OctetString(sp.getStart().getOctetString().getOctets())); 
            structure.addDataType(new OctetString(new byte[]{sp.getWeek()}));
            array.addDataType(structure);
        }
        return array;
    }
    
    public Array weekProfileTableActive() {
        Array array = new Array();
        List weekProfiles = messageActivityCalendar.getActiveWeekProfiles();
        Iterator it = weekProfiles.iterator();
        while(it.hasNext()) {
            Structure structure = new Structure();
            WeekProfile wp = (WeekProfile)it.next();
            structure.addDataType(new OctetString(new byte[]{wp.getName()}));
            structure.addDataType(new Unsigned8(wp.getMonday()));
            structure.addDataType(new Unsigned8(wp.getTuesday()));
            structure.addDataType(new Unsigned8(wp.getWednesday()));
            structure.addDataType(new Unsigned8(wp.getThursday()));
            structure.addDataType(new Unsigned8(wp.getFriday()));
            structure.addDataType(new Unsigned8(wp.getSaturday()));
            structure.addDataType(new Unsigned8(wp.getSunday()));
            array.addDataType(structure);
        }
        return array;
    }
    
    public Array dayProfileTableActive() {
        Structure structureDay = new Structure();
        Array arrayDay = new Array();
        List dayProfiles = messageActivityCalendar.getActiveDayProfiles();
        Iterator it = dayProfiles.iterator();
        while(it.hasNext()) {
            DayProfile dp = (DayProfile)it.next();
            structureDay.addDataType(new Unsigned8(dp.getDayId()));
            Array array = new Array();
            Iterator itSegments = dp.getSegments().iterator();
            while(itSegments.hasNext()) {
                DayProfileSegment dps = (DayProfileSegment)itSegments.next();
                Structure structure = new Structure();
                structure.addDataType(new OctetString(dps.getStartTimeOctets()));
                structure.addDataType(new OctetString(dps.getAction().getLogicalNameOctets()));
                structure.addDataType(new Unsigned16(dps.getAction().getSelector()));
                array.addDataType(structure);
            }
            structureDay.addDataType(array);
            arrayDay.addDataType(structureDay);
        }
        return arrayDay;
    }
    
    public OctetString calendarNamePassive() {
        return new OctetString(new byte[]{messageActivityCalendar.getPassiveCalendarName()});         
    }
    public Array seasonProfilePassive() {
        Array array = new Array();
        List seasonProfiles = messageActivityCalendar.getPassiveSeasonProfiles();
        Iterator it = seasonProfiles.iterator();
        while(it.hasNext()) {
            Structure structure = new Structure();
            SeasonProfile sp = (SeasonProfile)it.next();
            structure.addDataType(new OctetString(new byte[]{sp.getName()}));
            structure.addDataType(new OctetString(sp.getStart().getOctetString().getOctets()));
            structure.addDataType(new OctetString(new byte[]{sp.getWeek()}));
            array.addDataType(structure);
        }
        return array;        
    }
    public Array weekProfileTablePassive() {
        Array array = new Array();
        List weekProfiles = messageActivityCalendar.getPassiveWeekProfiles();
        Iterator it = weekProfiles.iterator();
        while(it.hasNext()) {
            Structure structure = new Structure();
            WeekProfile wp = (WeekProfile)it.next();
            structure.addDataType(new OctetString(new byte[]{wp.getName()}));
            structure.addDataType(new Unsigned8(wp.getMonday()));
            structure.addDataType(new Unsigned8(wp.getTuesday()));
            structure.addDataType(new Unsigned8(wp.getWednesday()));
            structure.addDataType(new Unsigned8(wp.getThursday()));
            structure.addDataType(new Unsigned8(wp.getFriday()));
            structure.addDataType(new Unsigned8(wp.getSaturday()));
            structure.addDataType(new Unsigned8(wp.getSunday()));
            array.addDataType(structure);
        }
        return array;        
    }
    public Array dayProfileTablePassive() {
        Structure structureDay = new Structure();
        Array arrayDay = new Array();
        List dayProfiles = messageActivityCalendar.getPassiveDayProfiles();
        Iterator it = dayProfiles.iterator();
        while(it.hasNext()) {
            DayProfile dp = (DayProfile)it.next();
            structureDay.addDataType(new Unsigned8(dp.getDayId()));
            Array array = new Array();
            Iterator itSegments = dp.getSegments().iterator();
            while(itSegments.hasNext()) {
                DayProfileSegment dps = (DayProfileSegment)itSegments.next();
                Structure structure = new Structure();
                structure.addDataType(new OctetString(dps.getStartTimeOctets()));
                structure.addDataType(new OctetString(dps.getAction().getLogicalNameOctets()));
                structure.addDataType(new Unsigned16(dps.getAction().getSelector()));
                array.addDataType(structure);
            }
            structureDay.addDataType(array);
        }
        return arrayDay;
    }
            
    public OctetString activatePassiveCalendarTime() {
        return new OctetString(messageActivityCalendar.getActivatePassiveCalendarTime().getOctetString().getOctets());     
    }
    
}
