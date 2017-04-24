/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class ReadDataFeature extends AbstractRadioCommand {

    public ReadDataFeature(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int featureData = 0;

    public int getFeatureData() {
        return featureData;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        featureData = ProtocolTools.getUnsignedIntFromBytes(data);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.ReadDataFeature;
    }

    public boolean isNoFlow() {
        return (featureData & 0x00000040) == 0x00000040;
    }

    public boolean isLeak() {
        return (featureData & 0x00000020) == 0x00000020;
    }

    public boolean isBurst() {
        return (featureData & 0x00000010) == 0x00000010;
    }

    public boolean isOverSpeed() {
        return (featureData & 0x00000008) == 0x00000008;
    }

    public boolean isBackFlow() {
        return (featureData & 0x00000004) == 0x00000004;
    }

    public int getAlarmDisplay() {
        return featureData & 0xFF;      //LSB
    }

    public int is7BandsEnabled() {
        return (featureData >> 30) & 0x01;
    }

    public int is4DaySegmentsEnabled() {
        return (featureData >> 29) & 0x01;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }

    public void setTariffMode(int enable) {
        featureData = featureData & 0xFFFEFFFF;
        featureData = featureData | enable << 16;
    }
}