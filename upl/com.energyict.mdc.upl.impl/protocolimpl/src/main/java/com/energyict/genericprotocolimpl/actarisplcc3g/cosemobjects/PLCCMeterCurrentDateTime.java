package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;


public class PLCCMeterCurrentDateTime extends AbstractPLCCObject {

    private Date date;
    private boolean dSTActive;

    public PLCCMeterCurrentDateTime(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }

    protected ObjectIdentification getId() {
        return new ObjectIdentification( "0.0.1.0.0.255", DLMSClassId.CLOCK.getClassId() );
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

    public void writeMeterClock(com.energyict.protocolimpl.edf.messages.objects.MeterClock meterClock) throws IOException {
        Calendar now = meterClock.getCosemCalendar().getCalendar();
        Clock clock = getCosemObjectFactory().getClock(getId().getObisCode());
        DateTime dt = new DateTime();
        dt.setValue(now, (byte)(dSTActive?128:0));
        getCosemObjectFactory().writeObject(ObisCode.fromString("0.0.1.0.0.255"),8,2, dt.getBEREncodedByteArray());
    }


    public com.energyict.protocolimpl.edf.messages.objects.MeterClock readMeterClock() throws IOException {
        Calendar calendar = ProtocolUtils.getCleanCalendar(getPLCCObjectFactory().getConcentrator().getTimeZone());
        calendar.setTime(getDate());
        com.energyict.protocolimpl.edf.messages.objects.MeterClock meterClock = new com.energyict.protocolimpl.edf.messages.objects.MeterClock(calendar, dSTActive);
        return meterClock;
    }

}