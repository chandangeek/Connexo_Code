package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.BcdEncodedField;
import com.energyict.protocolimplv2.abnt.common.frame.ResponseFrame;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;
import com.energyict.protocolimplv2.abnt.common.structure.field.LoadProfileWords;

import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class LoadProfileReadoutResponse extends Data<LoadProfileReadoutResponse> {

    private static final int BLOCK_COUNT_LENGTH = 2;

    private BcdEncodedField blockCount;
    private LoadProfileWords loadProfileWords;

    public LoadProfileReadoutResponse(TimeZone timeZone) {
        super(ResponseFrame.RESPONSE_DATA_LENGTH, timeZone);
        this.blockCount = new BcdEncodedField(BLOCK_COUNT_LENGTH);
        this.loadProfileWords = new LoadProfileWords();
    }

    @Override
    public LoadProfileReadoutResponse parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;
        super.parse(rawData, ptr);

        blockCount.parse(rawData, ptr);
        ptr += blockCount.getLength();

        loadProfileWords.parse(rawData, ptr);
        return this;
    }

    public BcdEncodedField getBlockCount() {
        return blockCount;
    }

    public LoadProfileWords getLoadProfileWords() {
        return loadProfileWords;
    }
}