package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.protocol.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;


public class PLCCCurrentDateTime extends AbstractPLCCObject {

    private DateTime dateTime;
    private boolean dSTActive;

    public PLCCCurrentDateTime(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }

    protected ObjectIdentification getId() {
        return new ObjectIdentification( "0.1.1.0.0.255", DLMSClassId.DATA.getClassId() );
    }

    protected void doInvoke() throws IOException {
    }

    public String toString() {
        return "PLCCCurrentDateTime: "+dateTime;
    }

    public Calendar getDateTime( ) throws IOException {
        if( dateTime == null ) {

            Data data = getCosemObjectFactory().getData( getObisCode() );

            TimeZone tz = getPLCCObjectFactory().getConcentrator().getTimeZone();
            dateTime = new DateTime( data.getRawValueAttr(), 0, tz );
            dSTActive = (dateTime.getStatus()&0x80)==0x80;

        }
        return dateTime.getValue();
    }

    public void setDateTime( ) throws IOException {
        Calendar now = Calendar.getInstance(getPLCCObjectFactory().getConcentrator().getTimeZone());
        dateTime = new DateTime();
        dateTime.setValue(now);
        dateTime.setValue( Calendar.getInstance() );
        byte [] ber = dateTime.getBEREncodedByteArray();
        getCosemObjectFactory().writeObject(getObisCode(), getClassId(), 2, ber);
    }

    public void writeMeterClock(com.energyict.protocolimpl.edf.messages.objects.MeterClock meterClock) throws IOException {
        Calendar now = meterClock.getCosemCalendar().getCalendar();
        dateTime = new DateTime();
        dateTime.setValue(now, (byte)(dSTActive?128:0));
        byte [] ber = dateTime.getBEREncodedByteArray();
        getCosemObjectFactory().writeObject(getObisCode(), getClassId(), 2, ber);

    }


    public com.energyict.protocolimpl.edf.messages.objects.MeterClock readMeterClock() throws IOException {
        Calendar calendar = ProtocolUtils.getCleanCalendar(getPLCCObjectFactory().getConcentrator().getTimeZone());
        calendar = getDateTime();
        com.energyict.protocolimpl.edf.messages.objects.MeterClock meterClock = new com.energyict.protocolimpl.edf.messages.objects.MeterClock(calendar, dSTActive);
        return meterClock;
    }

}