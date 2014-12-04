package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.frame.RequestFrame;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;

import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class PowerFailLogRequest extends Data<PowerFailLogRequest> {

    public PowerFailLogRequest(TimeZone timeZone) {
        super(RequestFrame.REQUEST_DATA_LENGTH, timeZone);
    }

    @Override
    public PowerFailLogRequest parse(byte[] rawData, int offset) throws ParsingException {
        // Nothing to parse, all bytes are NULL (0x00)
        return this;
    }
}