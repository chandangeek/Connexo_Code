package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import com.energyict.obis.*;
import com.energyict.protocol.*;
import com.energyict.dlms.cosem.AbstractCosemObject;
import java.io.IOException;
import java.util.*;
import java.util.Calendar;
import java.util.TimeZone;

import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.genericprotocolimpl.actarisplcc3g.Concentrator;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.ObjectIdentification;
import com.energyict.dlms.cosem.Clock;


public class PLCCMeterCurrentDateTime extends AbstractPLCCObject {
    
    private Date date;
    private boolean dSTActive;
    
    public PLCCMeterCurrentDateTime(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }
    
    protected ObjectIdentification getId() {
        return new ObjectIdentification( "0.0.1.0.0.255", AbstractCosemObject.CLASSID_CLOCK );
    }
    
    protected void doInvoke() throws IOException {
    }
    
    public String toString() {
        try {
            return "PLCCMeterCurrentDateTime: "+getDate();
        }
        catch(IOException e) {
            return e.toString();
        }
        
    }
    
    // this broadcasts a timesync to all meters...
    public void syncTime()  throws IOException {
        Clock clock = getCosemObjectFactory().getClock(getId().getObisCode());
        clock.invoke(129);
    }
    
    public Date getDate( ) throws IOException {
        Clock clock = getCosemObjectFactory().getClock(getId().getObisCode());
        date = clock.getDateTime();
        dSTActive = clock.getDstFlag()==1;
        return date;
    }

    public void setDate(Date date) throws IOException {
        Calendar now = Calendar.getInstance(getPLCCObjectFactory().getConcentrator().getTimeZone());
        Clock clock = getCosemObjectFactory().getClock(getId().getObisCode());
        DateTime dt = new DateTime();
        dt.setValue(now, (byte)(dSTActive?128:0));
        getCosemObjectFactory().writeObject(ObisCode.fromString("0.0.1.0.0.255"),8,2, dt.getBEREncodedByteArray());
    }
    
    public void writeMeterClock(com.energyict.edf.messages.objects.MeterClock meterClock) throws IOException {
        Calendar now = meterClock.getCosemCalendar().getCalendar();
        Clock clock = getCosemObjectFactory().getClock(getId().getObisCode());
        DateTime dt = new DateTime();
        dt.setValue(now, (byte)(dSTActive?128:0));
        getCosemObjectFactory().writeObject(ObisCode.fromString("0.0.1.0.0.255"),8,2, dt.getBEREncodedByteArray());
    }
    
    
    public com.energyict.edf.messages.objects.MeterClock readMeterClock() throws IOException {
        Calendar calendar = ProtocolUtils.getCleanCalendar(getPLCCObjectFactory().getConcentrator().getTimeZone());
        calendar.setTime(getDate());
        com.energyict.edf.messages.objects.MeterClock meterClock = new com.energyict.edf.messages.objects.MeterClock(calendar, dSTActive);
        return meterClock;
    }       

}