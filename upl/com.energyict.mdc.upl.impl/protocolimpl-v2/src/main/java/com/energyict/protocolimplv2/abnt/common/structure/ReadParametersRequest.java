package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
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
public class ReadParametersRequest extends Data<ReadParametersRequest> {

    private static final int PADDING_DATA_LENGTH = 57;

    private BcdEncodedField loadProfileBlockArgument;
    private BcdEncodedField channelGroupVisibility;
    private BcdEncodedField loadProfileReadSizeArgument;
    private NullData nullData;

    public ReadParametersRequest(TimeZone timeZone) {
        super(RequestFrame.REQUEST_DATA_LENGTH, timeZone);
        this.loadProfileBlockArgument = new BcdEncodedField();
        this.channelGroupVisibility = new BcdEncodedField();
        this.loadProfileReadSizeArgument = new BcdEncodedField();
        this.nullData = new NullData(PADDING_DATA_LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                loadProfileBlockArgument.getBytes(),
                channelGroupVisibility.getBytes(),
                loadProfileReadSizeArgument.getBytes(),
                nullData.getBytes()
        );
    }

    @Override
    public ReadParametersRequest parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.loadProfileBlockArgument.parse(rawData, ptr);
        ptr += this.loadProfileBlockArgument.getLength();

        this.channelGroupVisibility.parse(rawData, ptr);
        ptr += this.channelGroupVisibility.getLength();

        this.loadProfileReadSizeArgument.parse(rawData, ptr);
        return this;
    }

    public BcdEncodedField getLoadProfileBlockArgument() {
        return loadProfileBlockArgument;
    }

    public BcdEncodedField getChannelGroupVisibility() {
        return channelGroupVisibility;
    }

    public BcdEncodedField getLoadProfileReadSizeArgument() {
        return loadProfileReadSizeArgument;
    }
}