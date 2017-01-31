/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavelog.core.radiocommand;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.wavelog.WaveLog;

import java.io.IOException;

public class WriteOutputs extends AbstractRadioCommand {


    public WriteOutputs(WaveLog waveLog, int output, int type, int level) {
        super(waveLog);
        this.outputs = (int) Math.pow(2, output - 1);
        this.type = type * outputs;
        this.level = level * outputs;
    }

    //Mask indicating which output levels should be written
    private int outputs = 0x01;

    //0 = Pulse, 1 = Permanent state
    private int type;

    private int level;


    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.WriteOutputs;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        if ((data[0] & 0xFF) == 0xFF) {
            throw new WaveFlowException("Error writing the index");
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) outputs, (byte) type, (byte) level};
    }
}