/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.OperatingMode;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class GlobalIndexReading extends AbstractRadioCommand {

    public GlobalIndexReading(WaveFlow waveFlow) {
        super(waveFlow);
    }


    public String toString() {
        return "GlobalIndexReading: reading A=" + readings[0] + ", reading B=" + readings[1] + (readings.length > 2 ? ", reading C=" + readings[2] + ", reading D=" + readings[3] : "");
    }

    /**
     * The encoder readings
     */
    private long[] readings = new long[4];  // indexes for input A,B,C* and D*   (*) C and D depending on the type of waveflow

    public final long[] getReadings() {
        return readings;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.GlobalIndexReading;
    }

    public void setReading(int port, int value) {
        readings[port] = value;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        OperatingMode operatingMode = new OperatingMode(super.getWaveFlow(), operationMode);
        parse(data, operatingMode.getNumberOfInputsUsed());
    }

    public void parse(byte[] data, int numberOfPorts) throws IOException {
        if (data.length > 8) {
            readings = new long[4];
        } else {
            readings = new long[2];
        }

        readings[0] = ProtocolTools.getIntFromBytes(data, 0, 4);           //The indexes are signed values!
        readings[1] = ProtocolTools.getIntFromBytes(data, 4, 4);

        boolean type4Inputs = (numberOfPorts == 4);

        if (data.length >= 12) {
            if (type4Inputs) {
                readings[2] = ProtocolTools.getIntFromBytes(data, 8, 4);                               //MSB first in this case
            } else {
                byte[] value = ProtocolTools.getSubArray(data, 8, 12);
                readings[2] = ProtocolTools.getIntFromBytes(ProtocolTools.reverseByteArray(value));    //LSB  first!
            }
        }
        if (data.length >= 16) {
            if (type4Inputs) {
                readings[3] = ProtocolTools.getIntFromBytes(data, 12, 4);
            } else {
                byte[] value = ProtocolTools.getSubArray(data, 12, 16);
                readings[3] = ProtocolTools.getIntFromBytes(ProtocolTools.reverseByteArray(value));
            }
        }
    }

    protected byte[] prepare() throws IOException {
        return new byte[0];
    }
}