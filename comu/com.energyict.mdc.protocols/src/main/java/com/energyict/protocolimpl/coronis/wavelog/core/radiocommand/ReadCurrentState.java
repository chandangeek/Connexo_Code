/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavelog.core.radiocommand;

import com.energyict.protocolimpl.coronis.wavelog.WaveLog;

import java.io.IOException;

public class ReadCurrentState extends AbstractRadioCommand {

    protected ReadCurrentState(WaveLog waveLog) {
        super(waveLog);
    }

    private int state;

    public int getState() {
        return state;
    }

    public int getInputState(int input) {
        switch (input) {
            case 0:
                return (state & 0x01);
            case 1:
                return (state & 0x02) >> 1;
            case 2:
                return (state & 0x04) >> 2;
            case 3:
                return (state & 0x08) >> 3;
            default:
                return 0;
        }
    }

    public int getOutputState(int output) {
        switch (output) {
            case 0:
                return (state & 0x10) >> 4;
            case 1:
                return (state & 0x20) >> 5;
            case 2:
                return (state & 0x40) >> 6;
            case 3:                 
                return (state & 0x80) >> 7;
            default:
                return 0;
        }
    }


    @Override
    protected void parse(byte[] data) throws IOException {
        int offset = 0;

        int status = data[offset] & 0xFF;
        offset++;

        state = data[offset] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ReadCurrentState;
    }
}