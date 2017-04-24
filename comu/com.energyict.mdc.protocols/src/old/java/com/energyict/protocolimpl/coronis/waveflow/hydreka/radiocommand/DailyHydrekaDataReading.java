/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.hydreka.radiocommand;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;
import com.energyict.protocolimpl.coronis.waveflow.hydreka.GenericHeader;

import java.io.IOException;
import java.util.Date;

public class DailyHydrekaDataReading extends AbstractRadioCommand {

    private Date leakageDetectionDate;
    private byte[] rawData;

    public DailyHydrekaDataReading(WaveFlow waveFlow) {
        super(waveFlow);
    }

    public Date getLeakageDetectionDate() {
        return leakageDetectionDate;
    }

    public byte[] getRawData() {
        return rawData;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        rawData = data;
        GenericHeader genericHeader = new GenericHeader(getWaveFlow());
        genericHeader.parse(data);  //Cache the meta data in the factories
        leakageDetectionDate = TimeDateRTCParser.parse(data, 56, 7, getWaveFlow().getTimeZone()).getTime();
    }

    public void parseFromBubbleUp(byte[] data) throws IOException {
        this.parse(data);   //Public accessor
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];    //No extra arguments
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.DailyConsumption;
    }
}