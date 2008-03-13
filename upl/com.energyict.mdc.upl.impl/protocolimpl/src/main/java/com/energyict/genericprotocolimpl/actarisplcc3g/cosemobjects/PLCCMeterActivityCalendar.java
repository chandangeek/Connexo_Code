package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import java.io.*;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.obis.*;
import com.energyict.protocol.*;
import com.energyict.dlms.cosem.ObjectIdentification;
import com.energyict.dlms.cosem.AbstractCosemObject;
import com.energyict.dlms.cosem.ActivityCalendar;

/**
 *
 * @author kvds
 */
public class PLCCMeterActivityCalendar extends AbstractPLCCObject {
    
    private ActivityCalendar activityCalendar=null; 
    
    /** Creates a new instance of PLCCMeterActivityCalendar */
    public PLCCMeterActivityCalendar(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }
    
    protected void doInvoke() throws IOException {
        activityCalendar = (getCosemObjectFactory().getActivityCalendar(getId().getObisCode()));
    }
    
    protected ObjectIdentification getId() {
        return new ObjectIdentification(ObisCode.fromString("0.0.13.0.0.255"), AbstractCosemObject.CLASSID_ACTIVITY_CALENDAR);
    }
    
    public ActivityCalendar getActivityCalendar() throws IOException {
        if (activityCalendar==null)
            invoke();
        return activityCalendar;
    }

    public com.energyict.edf.messages.objects.ActivityCalendar readActivityCalendar() throws IOException {
        // convert to message cosem object to write into a register...
        ActivityCalendarBuilder activityCalendarBuilder = new ActivityCalendarBuilder(this);
        return activityCalendarBuilder.toActivityCalendar();
    }    
    
    public void writeActivityCalendar(com.energyict.edf.messages.objects.ActivityCalendar ac) throws IOException {
        CosemActivityCalendarBuilder cosemActivityCalendarBuilder = new CosemActivityCalendarBuilder(ac);
        //activityCalendar.writeCalendarNameActive(cosemActivityCalendarBuilder.calendarNameActive());
        activityCalendar.writeCalendarNamePassive(cosemActivityCalendarBuilder.calendarNamePassive());
        //activityCalendar.writeDayProfileTableActive(cosemActivityCalendarBuilder.dayProfileTableActive());
        activityCalendar.writeDayProfileTablePassive(cosemActivityCalendarBuilder.dayProfileTablePassive());
        //activityCalendar.writeWeekProfileTableActive(cosemActivityCalendarBuilder.weekProfileTableActive());
        activityCalendar.writeWeekProfileTablePassive(cosemActivityCalendarBuilder.weekProfileTablePassive());
        //activityCalendar.writeSeasonProfileActive(cosemActivityCalendarBuilder.seasonProfileActive());
        activityCalendar.writeSeasonProfilePassive(cosemActivityCalendarBuilder.seasonProfilePassive());
        
        activityCalendar.writeActivatePassiveCalendarTime(cosemActivityCalendarBuilder.activatePassiveCalendarTime());
    }
    
}
