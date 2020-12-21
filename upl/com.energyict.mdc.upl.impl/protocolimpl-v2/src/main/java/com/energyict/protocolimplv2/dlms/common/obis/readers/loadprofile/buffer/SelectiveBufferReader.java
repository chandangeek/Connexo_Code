package com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.buffer;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimpl.dlms.as220.ProfileLimiter;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.io.IOException;
import java.util.Calendar;

public class SelectiveBufferReader implements BufferReader {

    private final int limitMaxNrOfDays;

    public SelectiveBufferReader(int limitMaxNrOfDays) {
        this.limitMaxNrOfDays = limitMaxNrOfDays;
    }

    @Override
    public DataContainer read(AbstractDlmsProtocol protocol, ObisCode deviceLoadProfileObisCode, LoadProfileReader loadProfileReader) throws IOException {
        ProfileGeneric profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(deviceLoadProfileObisCode, protocol.useDsmr4SelectiveAccessFormat());
        Calendar fromCalendar = getFromCalendar(loadProfileReader, protocol);
        Calendar toCalendar = getToCalendar(loadProfileReader, protocol);
        // reading captured objects is needed while later requests are constructed based on it...
        profileGeneric.getCaptureObjects();
        return profileGeneric.getBuffer(fromCalendar, toCalendar);
    }

    private Calendar getFromCalendar(com.energyict.protocol.LoadProfileReader loadProfileReader, AbstractDlmsProtocol protocol) {
        ProfileLimiter profileLimiter = new ProfileLimiter(loadProfileReader.getStartReadingTime(), loadProfileReader.getEndReadingTime(), limitMaxNrOfDays);
        Calendar fromCal = Calendar.getInstance(protocol.getTimeZone());
        fromCal.setTime(profileLimiter.getFromDate());
        fromCal.set(Calendar.SECOND, 0);
        return fromCal;
    }

    private Calendar getToCalendar(com.energyict.protocol.LoadProfileReader loadProfileReader, AbstractDlmsProtocol protocol) {
        ProfileLimiter profileLimiter = new ProfileLimiter(loadProfileReader.getStartReadingTime(), loadProfileReader.getEndReadingTime(), limitMaxNrOfDays);
        Calendar toCal = Calendar.getInstance(protocol.getTimeZone());
        toCal.setTime(profileLimiter.getToDate());
        toCal.set(Calendar.SECOND, 0);
        return toCal;
    }
}
