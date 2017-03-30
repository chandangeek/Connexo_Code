/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure;

import com.energyict.protocolimplv2.elster.garnet.RequestFactory;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Data;
import com.energyict.protocolimplv2.elster.garnet.frame.field.FunctionCode;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class RadioParametersRequestStructure extends Data<RadioParametersRequestStructure> {

    public static final int PADDING_DATA_LENGTH = 3;
    public static final FunctionCode FUNCTION_CODE = FunctionCode.CONCENTRATOR_STATUS_REQUEST;

    private PaddingData paddingData;
    private RequestFactory requestFactory;

    public RadioParametersRequestStructure(RequestFactory requestFactory) {
        super(FUNCTION_CODE);
        this.requestFactory = requestFactory;
        this.paddingData = new PaddingData(PADDING_DATA_LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return paddingData.getBytes();
    }

    @Override
    public RadioParametersRequestStructure parse(byte[] rawData, int offset) throws ParsingException {
        this.paddingData.parse(rawData, offset);
        return this;
    }

    private RequestFactory getRequestFactory() {
        return requestFactory;
    }

    public PaddingData getPaddingData() {
        return paddingData;
    }
}