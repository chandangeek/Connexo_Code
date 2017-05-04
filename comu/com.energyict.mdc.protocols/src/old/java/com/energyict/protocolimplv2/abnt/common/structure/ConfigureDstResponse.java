/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.frame.RequestFrame;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;
import com.energyict.protocolimplv2.abnt.common.structure.field.DstConfigurationRecord;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;

import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class ConfigureDstResponse extends Data<ConfigureDstResponse> {

    private static final int PADDING_DATA_LENGTH = 246;


    private DstConfigurationRecord dstConfigurationRecord;

    private PaddingData paddingData;

    public ConfigureDstResponse(TimeZone timeZone) {
        super(RequestFrame.REQUEST_DATA_LENGTH, timeZone);
        this.dstConfigurationRecord = new DstConfigurationRecord();
        this.paddingData = new PaddingData(PADDING_DATA_LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                dstConfigurationRecord.getBytes(),
                paddingData.getBytes()
        );
    }

    @Override
    public ConfigureDstResponse parse(byte[] rawData, int offset) throws ParsingException {
        super.parse(rawData, offset);
        dstConfigurationRecord.parse(rawData, offset);
        return this;
    }

    public DstConfigurationRecord getDstConfigurationRecord() {
        return dstConfigurationRecord;
    }
}