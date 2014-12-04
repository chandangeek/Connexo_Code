package com.energyict.protocolimplv2.elster.garnet.structure.logbook;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class SimpleEvent extends AbstractField<SimpleEvent> implements  LogBookEvent{

    public static final int LENGTH = 16;

    private final LogBookEventCode logBookEventCode;
    private PaddingData paddingData;

    public SimpleEvent(LogBookEventCode logBookEventCode) {
        this.logBookEventCode = logBookEventCode;
        this.paddingData = new PaddingData(16);
    }

    @Override
    public byte[] getBytes() {
        return paddingData.getBytes();
    }

    public SimpleEvent parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.paddingData.parse(rawData, ptr);
        ptr += paddingData.getLength();

        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    @Override
    public String getEventDescription() {
        return "Unknown event " + ProtocolTools.getHexStringFromInt(logBookEventCode.getEventCode().getCode(), 1, "0x");
    }

    public PaddingData getPaddingData() {
        return paddingData;
    }
}