package com.energyict.protocolimplv2.dlms.as3000.custom;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.protocol.exception.CommunicationException;

import java.io.IOException;

public class ComposedMeterInfo extends com.energyict.protocolimplv2.common.composedobjects.ComposedMeterInfo {

    private final DLMSAttribute serialNr;

    public ComposedMeterInfo(ProtocolLink dlmsSession, boolean bulkRequest, int roundTripCorrection, int retries, DLMSAttribute serialnr, DLMSAttribute clock) {
        super(dlmsSession, bulkRequest, roundTripCorrection, retries, serialnr, clock);
        this.serialNr = serialnr;
    }

    @Override
    public String getSerialNr() {
        AbstractDataType attribute = getAttribute(serialNr);
        if (attribute instanceof Unsigned32) {
            return Long.valueOf(attribute.getUnsigned32().getValue()).toString();
        } else {
            IOException ioException = new IOException("Expected Unsigned32 but was " + attribute.getClass().getSimpleName());
            throw CommunicationException.unexpectedResponse(ioException);
        }
    }
}
