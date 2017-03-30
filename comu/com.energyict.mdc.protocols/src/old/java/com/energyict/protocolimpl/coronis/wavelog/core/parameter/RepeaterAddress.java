/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavelog.core.parameter;

import com.energyict.protocolimpl.coronis.wavelog.WaveLog;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class RepeaterAddress extends AbstractParameter {

    String address;
    int id;

    RepeaterAddress(WaveLog waveLog) {
        super(waveLog);
    }

    public RepeaterAddress(WaveLog waveLog, int id) {
        super(waveLog);
        this.id = id;
    }

    @Override
    ParameterId getParameterId() {
        switch (id) {
            default:
            case 1:
                return ParameterId.RepeaterAddress1;
            case 2:
                return ParameterId.RepeaterAddress2;
            case 3:
                return ParameterId.RepeaterAddress3;
        }
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        address = ProtocolTools.getHexStringFromBytes(data);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return ProtocolTools.getBytesFromHexString(address, "");
    }

    public void setAddress(String address) {
        this.address = address;
    }
}