package com.energyict.protocolimplv2.dlms.common.writers.converters;

import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.util.Calendar;
import java.util.TimeZone;

public class AXRDateTimeConverter extends AbstractConverter {


    public AXRDateTimeConverter(String attName) {
        super(attName);
    }

    @Override
    public byte[] convert(AbstractDlmsProtocol dlmsProtocol, OfflineDeviceMessage message) throws ProtocolException {
        long value = Long.parseLong(super.getAttValue(message));
        TimeZone timeZone = dlmsProtocol.getTimeZone();
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTimeInMillis(value);
        return new AXDRDateTime(cal.getTime(), timeZone).getBEREncodedByteArray();
    }
}
