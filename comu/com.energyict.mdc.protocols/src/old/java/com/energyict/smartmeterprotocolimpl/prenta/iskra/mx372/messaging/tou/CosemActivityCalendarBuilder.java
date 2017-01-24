package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.messaging.tou;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.OctetString;

import java.util.Iterator;
import java.util.List;

public class CosemActivityCalendarBuilder {
    
    ActivityCalendar messageActivityCalendar;
            
    /** Creates a new instance of CosemActivityCalendarBuilder */
    public CosemActivityCalendarBuilder(ActivityCalendar messageActivityCalendar) {
        this.messageActivityCalendar=messageActivityCalendar;
    }
    
    public OctetString calendarNameActive() {
        return OctetString.fromByteArray(new byte[]{messageActivityCalendar.getActiveCalendarName()});
    }
    public Array seasonProfileActive() {
        Array array = new Array();
        List seasonProfiles = messageActivityCalendar.getActiveSeasonProfiles();
        Iterator it = seasonProfiles.iterator();
        while(it.hasNext()) {
            Structure structure = new Structure();
            SeasonProfile sp = (SeasonProfile)it.next();
            structure.addDataType(OctetString.fromByteArray(new byte[]{sp.getName()}));
            structure.addDataType(OctetString.fromByteArray(sp.getStart().getOctetString().getOctets()));
            structure.addDataType(OctetString.fromByteArray(new byte[]{sp.getWeek()}));
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
            structure.addDataType(OctetString.fromByteArray(new byte[]{wp.getName()}));
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
                structure.addDataType(OctetString.fromByteArray(dps.getStartTimeOctets()));
                structure.addDataType(OctetString.fromByteArray(dps.getAction().getLogicalNameOctets()));
                structure.addDataType(new Unsigned16(dps.getAction().getSelector()));
                array.addDataType(structure);
            }
            structureDay.addDataType(array);
            arrayDay.addDataType(structureDay);
        }
        return arrayDay;
    }
    
    public OctetString calendarNamePassive() {
        return OctetString.fromByteArray(new byte[]{messageActivityCalendar.getPassiveCalendarName()});
    }
    public Array seasonProfilePassive() {
        Array array = new Array();
        List seasonProfiles = messageActivityCalendar.getPassiveSeasonProfiles();
        Iterator it = seasonProfiles.iterator();
        while(it.hasNext()) {
            Structure structure = new Structure();
            SeasonProfile sp = (SeasonProfile)it.next();
            structure.addDataType(OctetString.fromByteArray(new byte[]{sp.getName()}));
            structure.addDataType(OctetString.fromByteArray(sp.getStart().getOctetString().getOctets()));
            structure.addDataType(OctetString.fromByteArray(new byte[]{sp.getWeek()}));
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
            structure.addDataType(OctetString.fromByteArray(new byte[]{wp.getName()}));
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
    
    
    public Array specialDays() {
    	Array specialDaysArray = new Array();
    	Iterator it = messageActivityCalendar.getSpecialDays().iterator();
    	while (it.hasNext()) {
    		SpecialDay specialDay = (SpecialDay) it.next();
    		Structure specialDayStructure = new Structure();
    		specialDayStructure.addDataType(new Unsigned16(specialDay.getIndex()));
    		specialDayStructure.addDataType(OctetString.fromByteArray(specialDay.getDateOctets()));
    		specialDayStructure.addDataType(new Unsigned8(specialDay.getDayId()));
    		specialDaysArray.addDataType(specialDayStructure);
    	}
    	return specialDaysArray;
    }
    
    
    public Array dayProfileTablePassive() {
        Array dayProfilesArray = new Array();
        List dayProfiles = messageActivityCalendar.getPassiveDayProfiles();
        Iterator it = dayProfiles.iterator();
        while(it.hasNext()) {
            Structure dayProfileStructure = new Structure();
            DayProfile dp = (DayProfile)it.next();
            dayProfileStructure.addDataType(new Unsigned8(dp.getDayId()));
            Array segmentsArray = new Array();
            Iterator itSegments = dp.getSegments().iterator();
            while(itSegments.hasNext()) {
                DayProfileSegment dps = (DayProfileSegment)itSegments.next();
                Structure segmentStructure = new Structure();
                segmentStructure.addDataType(OctetString.fromByteArray(dps.getStartTimeOctets()));
                segmentStructure.addDataType(OctetString.fromByteArray(dps.getAction().getLogicalNameOctets()));
                segmentStructure.addDataType(new Unsigned16(dps.getAction().getSelector()));
                segmentsArray.addDataType(segmentStructure);
            }
            dayProfileStructure.addDataType(segmentsArray);
            dayProfilesArray.addDataType(dayProfileStructure);
        }
        return dayProfilesArray;
    }
            
    public OctetString activatePassiveCalendarTime() {
        return OctetString.fromByteArray(messageActivityCalendar.getActivatePassiveCalendarTime().getOctetString().getOctets());
    }
    
}
