/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;
import com.energyict.protocolimplv2.abnt.common.structure.field.LoadProfileWords;

import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class LoadProfileReadoutResponse extends Data<LoadProfileReadoutResponse> {

    private static final int BLOCK_COUNT_LENGTH = 2;

    private LoadProfileWords loadProfileWords;

    public LoadProfileReadoutResponse(TimeZone timeZone, int length) {
        super(length, timeZone);
        this.loadProfileWords = new LoadProfileWords(length);
    }

    @Override
    public LoadProfileReadoutResponse parse(byte[] rawData, int offset) throws ParsingException {
        super.parse(rawData, offset);
        loadProfileWords.parse(rawData, offset);
        return this;
    }

    public LoadProfileWords getLoadProfileWords() {
        return loadProfileWords;
    }
}