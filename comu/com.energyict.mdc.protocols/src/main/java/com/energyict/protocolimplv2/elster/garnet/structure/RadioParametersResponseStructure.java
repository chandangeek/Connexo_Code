package com.energyict.protocolimplv2.elster.garnet.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Data;
import com.energyict.protocolimplv2.elster.garnet.frame.field.FunctionCode;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class RadioParametersResponseStructure extends Data<RadioParametersResponseStructure> {

    public static final int RADIO_NET_LENGTH = 2;
    public static final int PADDING_DATA_LENGTH = 22;
    public static final FunctionCode FUNCTION_CODE = FunctionCode.CONCENTRATOR_STATUS_RESPONSE;

    private int radioNET;
    private PaddingData paddingData;

    public RadioParametersResponseStructure() {
        super(FUNCTION_CODE);
        this.radioNET = 0;
        this.paddingData = new PaddingData(PADDING_DATA_LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return  ProtocolTools.concatByteArrays(
                getBytesFromIntLE(radioNET, RADIO_NET_LENGTH),
                paddingData.getBytes()
        );
    }

    @Override
    public RadioParametersResponseStructure parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.radioNET = getIntFromBytesLE(rawData, ptr, RADIO_NET_LENGTH);
        ptr += RADIO_NET_LENGTH;

        this.paddingData.parse(rawData, ptr);
        return this;
    }

    public int getRadioNET() {
        return radioNET;
    }

    public PaddingData getPaddingData() {
        return paddingData;
    }
}