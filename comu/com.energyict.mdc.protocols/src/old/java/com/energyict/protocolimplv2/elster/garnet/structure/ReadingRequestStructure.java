/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.RequestFactory;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Data;
import com.energyict.protocolimplv2.elster.garnet.frame.field.FunctionCode;
import com.energyict.protocolimplv2.elster.garnet.structure.field.DateTime;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;
import com.energyict.protocolimplv2.elster.garnet.structure.field.ReadingSelector;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class ReadingRequestStructure extends Data<ReadingRequestStructure> {

    public static final FunctionCode FUNCTION_CODE = FunctionCode.CHECKPOINT_READING_REQUEST;
    private static final int PADDING_DATA_LENGTH = 7;

    private DateTime dateTime;
    private ReadingSelector readingSelector;
    private PaddingData paddingData;
    private RequestFactory requestFactory;

    public enum ReadingMode {
        CHECKPOINT_READING,
        ONLINE_READING
    }

    public ReadingRequestStructure(RequestFactory requestFactory) {
        super(FUNCTION_CODE);
        this.requestFactory = requestFactory;
        this.dateTime = new DateTime(requestFactory.getClock(), requestFactory.getTimeZone());
        this.readingSelector = new ReadingSelector();
        this.paddingData = new PaddingData(PADDING_DATA_LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                dateTime.getBytes(),
                readingSelector.getBytes(),
                paddingData.getBytes()
        );
    }

    @Override
    public ReadingRequestStructure parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.dateTime = new DateTime(requestFactory.getClock(), requestFactory.getTimeZone()).parse(rawData, ptr);
        ptr += dateTime.getLength();

        this.readingSelector = new ReadingSelector().parse(rawData, ptr);
        ptr += readingSelector.getLength();

        this.paddingData = new PaddingData(PADDING_DATA_LENGTH).parse(rawData, ptr);
        return this;
    }

    private RequestFactory getRequestFactory() {
        return requestFactory;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public ReadingSelector getReadingSelector() {
        return readingSelector;
    }

    public void setReadingSelector(ReadingSelector readingSelector) {
        this.readingSelector = readingSelector;
    }

    public PaddingData getPaddingData() {
        return paddingData;
    }

    public void setPaddingData(PaddingData paddingData) {
        this.paddingData = paddingData;
    }
}