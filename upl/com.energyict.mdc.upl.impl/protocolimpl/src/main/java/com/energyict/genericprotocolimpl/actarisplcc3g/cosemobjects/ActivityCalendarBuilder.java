/*
 * ActivityCalendarBuilder.java
 *
 * Created on 7 december 2007, 17:45
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.protocolimpl.edf.messages.objects.*;

import java.io.IOException;
import java.util.*;
/**
 *
 * @author kvds
 */
public class ActivityCalendarBuilder {
    
    PLCCMeterActivityCalendar pLCCMeterActivityCalendar;
    ActivityCalendar activityCalendar;
    /** Creates a new instance of ActivityCalendarBuilder */
    public ActivityCalendarBuilder(PLCCMeterActivityCalendar pLCCMeterActivityCalendar) {
        this.pLCCMeterActivityCalendar=pLCCMeterActivityCalendar;
        
    }
    
    public ActivityCalendar toActivityCalendar() throws IOException {
        activityCalendar = new ActivityCalendar();
        buildActiveCalendarName();
        activityCalendar.setActiveSeasonProfiles(buildSeasonProfiles(pLCCMeterActivityCalendar.getActivityCalendar().readSeasonProfileActive()));
        activityCalendar.setActiveWeekProfiles(buildWeekProfiles(pLCCMeterActivityCalendar.getActivityCalendar().readWeekProfileTableActive()));
        activityCalendar.setActiveDayProfiles(buildDayProfiles(pLCCMeterActivityCalendar.getActivityCalendar().readDayProfileTableActive()));
        buildPassiveCalendarName();
        activityCalendar.setPassiveSeasonProfiles(buildSeasonProfiles(pLCCMeterActivityCalendar.getActivityCalendar().readSeasonProfilePassive()));
        activityCalendar.setPassiveWeekProfiles(buildWeekProfiles(pLCCMeterActivityCalendar.getActivityCalendar().readWeekProfileTablePassive()));
        activityCalendar.setPassiveDayProfiles(buildDayProfiles(pLCCMeterActivityCalendar.getActivityCalendar().readDayProfileTablePassive()));
        buildActivatePassiveCalendarTime();
        return activityCalendar;
    }
    
    private void buildActivatePassiveCalendarTime() throws IOException {
        com.energyict.protocolimpl.edf.messages.objects.OctetString o = new com.energyict.protocolimpl.edf.messages.objects.OctetString(pLCCMeterActivityCalendar.getActivityCalendar().readActivatePassiveCalendarTime().getOctetStr());
        activityCalendar.setActivatePassiveCalendarTime(new CosemCalendar(o));
    }
    
    private void buildActiveCalendarName() throws IOException {
        activityCalendar.setActiveCalendarName(pLCCMeterActivityCalendar.getActivityCalendar().readCalendarNameActive().getOctetStr()[0]);
    }
    private void buildPassiveCalendarName() throws IOException {
        activityCalendar.setPassiveCalendarName(pLCCMeterActivityCalendar.getActivityCalendar().readCalendarNamePassive().getOctetStr()[0]);
    }
    private List  buildSeasonProfiles(Array array) throws IOException {
        List seasonProfiles = new ArrayList();
        for (int index=0;index<array.nrOfDataTypes();index++) {
            Structure structure = array.getDataType(index).getStructure();
            SeasonProfile seasonProfile = new SeasonProfile();
            seasonProfile.setName(structure.getDataType(0).getOctetString().getOctetStr()[0]);
            TimeZone timeZone = pLCCMeterActivityCalendar.getPLCCObjectFactory().getConcentrator().getTimeZone();       
            com.energyict.protocolimpl.edf.messages.objects.OctetString o = new com.energyict.protocolimpl.edf.messages.objects.OctetString(structure.getDataType(1).getOctetString().getOctetStr());
            seasonProfile.setStart(new CosemCalendar(o));
            seasonProfile.setWeek(structure.getDataType(2).getOctetString().getOctetStr()[0]);
            seasonProfiles.add(seasonProfile);
        }
        return seasonProfiles;
    }
    
    private List buildWeekProfiles(Array array) throws IOException {
        List weekProfiles = new ArrayList();
        for (int index=0;index<array.nrOfDataTypes();index++) {
            Structure structure = array.getDataType(index).getStructure();
            WeekProfile weekProfile = new WeekProfile();
            weekProfile.setName(structure.getDataType(0).getOctetString().getOctetStr()[0]);
            weekProfile.setMonday(structure.getDataType(1).intValue());
            weekProfile.setTuesday(structure.getDataType(2).intValue());
            weekProfile.setWednesday(structure.getDataType(3).intValue());
            weekProfile.setThursday(structure.getDataType(4).intValue());
            weekProfile.setFriday(structure.getDataType(5).intValue());
            weekProfile.setSaturday(structure.getDataType(6).intValue());
            weekProfile.setSunday(structure.getDataType(7).intValue());
            weekProfiles.add(weekProfile);
        }
        return weekProfiles;
    }

    // demo
    private List buildDayProfilesDemo(Array array) throws IOException {
        List dayProfiles = new ArrayList();
        for (int index=0;index<1;index++) {
            DayProfile dayProfile = new DayProfile();
            dayProfile.setDayId(1);
            List dayProfileSegments = new ArrayList();
            
            DayProfileSegment dayProfileSegment = new DayProfileSegment();
            dayProfileSegment.setStartTimeOctects(new byte[]{0,0,0,0});
            ActionItem action = new ActionItem();
            action.setLogicalNameOctets(new byte[]{0,0,0,0,0,0});
            action.setSelector(1);
            dayProfileSegment.setAction(action);
            dayProfileSegments.add(dayProfileSegment);

            dayProfileSegment = new DayProfileSegment();
            dayProfileSegment.setStartTimeOctects(new byte[]{6,0,0,0});
            action = new ActionItem();
            action.setLogicalNameOctets(new byte[]{0,0,0,0,0,0});
            action.setSelector(2);
            dayProfileSegment.setAction(action);
            dayProfileSegments.add(dayProfileSegment);
            
            dayProfileSegment = new DayProfileSegment();
            dayProfileSegment.setStartTimeOctects(new byte[]{16,0,0,0});
            action = new ActionItem();
            action.setLogicalNameOctets(new byte[]{0,0,0,0,0,0});
            action.setSelector(1);
            dayProfileSegment.setAction(action);
            dayProfileSegments.add(dayProfileSegment);
            
            
            dayProfile.setSegments(dayProfileSegments);
            dayProfiles.add(dayProfile);
        }
        return dayProfiles;
    }
    
    private List buildDayProfiles(Array array) throws IOException {
        List dayProfiles = new ArrayList();
        for (int index=0;index<array.nrOfDataTypes();index++) {
            Structure structure = array.getDataType(index).getStructure();
            DayProfile dayProfile = new DayProfile();
            dayProfile.setDayId(structure.getDataType(0).intValue());
            
            Array arraySegments = structure.getDataType(1).getArray();
            List dayProfileSegments = new ArrayList();
            for (int segment=0;segment<arraySegments.nrOfDataTypes();segment++) {
                Structure structureSegment = arraySegments.getDataType(segment).getStructure();
                DayProfileSegment dayProfileSegment = new DayProfileSegment();
                dayProfileSegment.setStartTimeOctects(structureSegment.getDataType(0).getOctetString().getOctetStr());
                ActionItem action = new ActionItem();
                action.setLogicalNameOctets(structureSegment.getDataType(1).getOctetString().getOctetStr());
                action.setSelector(structureSegment.getDataType(2).intValue());
                dayProfileSegment.setAction(action);
                dayProfileSegments.add(dayProfileSegment);
            }
            
            
            dayProfile.setSegments(dayProfileSegments);
            dayProfiles.add(dayProfile);
        }
        return dayProfiles;
    }
    
    
    
}
