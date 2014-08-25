package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.RequestFactory;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.BcdEncodedField;
import com.energyict.protocolimplv2.abnt.common.field.NullData;
import com.energyict.protocolimplv2.abnt.common.frame.RequestFrame;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;

import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class PowerFailLogRequest extends Data<PowerFailLogRequest> {

    private static final int PADDING_DATA_LENGTH = 59;

    private BcdEncodedField selector;
    private NullData nullData;
    private RequestFactory requestFactory;

    public PowerFailLogRequest(TimeZone timeZone) {
        super(RequestFrame.REQUEST_DATA_LENGTH, timeZone);
        this.selector = new BcdEncodedField();
        this.nullData = new NullData(PADDING_DATA_LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                selector.getBytes(),
                nullData.getBytes()
        );
    }

    @Override
    public PowerFailLogRequest parse(byte[] rawData, int offset) throws ParsingException {
        this.selector.parse(rawData, offset);
        return this;
    }

    public BcdEncodedField getSelector() {
        return selector;
    }

    private RequestFactory getRequestFactory() {
        return requestFactory;
    }
}