/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class VoltageRequest extends AbstractRadioCommand {

    private double voltageVPAL;
    private double voltageV1;
    private double voltageV2;

    private static final double ROUNDING_DOUBLE = 10000.0;
    private static final byte[] SUBCOMMAND = new byte[]{(byte) 0x09};
    private static final byte[] SM150E_ADDRESS = new byte[]{(byte) 0x03, (byte) 0x8C, (byte) 0x06};
    private static final byte[] ECHODIS_ADDRESS = new byte[]{(byte) 0x03, (byte) 0x8E, (byte) 0x06};

    @Override
    EncoderRadioCommandId getEncoderRadioCommandId() {
        return EncoderRadioCommandId.VoltageRequest;
    }

    VoltageRequest(WaveFlow100mW waveFlow100mW) {
        super(waveFlow100mW);
    }

    public double getVoltageVPAL() {
        return Math.round(voltageVPAL * ROUNDING_DOUBLE) / ROUNDING_DOUBLE;
    }

    public double getVoltageV1() {
        return Math.round(voltageV1 * ROUNDING_DOUBLE) / ROUNDING_DOUBLE;
    }

    public double getVoltageV2() {
        return Math.round(voltageV2 * ROUNDING_DOUBLE) / ROUNDING_DOUBLE;
    }

    @Override
    void parse(byte[] data) throws IOException {
        int expectedTag = SUBCOMMAND[0] | 0x80;
        int receivedTag = data[0] & 0xFF;
        if (receivedTag != expectedTag) {
            throw new WaveFlow100mwEncoderException("Invalid subcommand response tag, expected [0x" + expectedTag + "], received [0x" + receivedTag + "]");
        }

        byte[] reversedData = ProtocolTools.getReverseByteArray(ProtocolTools.getSubArray(data, 1));
        voltageVPAL = ((double) ProtocolTools.getIntFromBytes(reversedData, 0, 2)) / 1105;
        voltageV2 = ((double) ProtocolTools.getIntFromBytes(reversedData, 2, 2)) / 1105;
        voltageV1 = ((double) ProtocolTools.getIntFromBytes(reversedData, 4, 2)) / 1105;
    }

    @Override
    byte[] prepare() throws IOException {
        if (getWaveFlow100mW().getMeterProtocolType() == WaveFlow100mW.MeterProtocolType.SM150E) {
            return ProtocolTools.concatByteArrays(SUBCOMMAND, SM150E_ADDRESS);
        }
        if (getWaveFlow100mW().getMeterProtocolType() == WaveFlow100mW.MeterProtocolType.ECHODIS) {
            return ProtocolTools.concatByteArrays(SUBCOMMAND, ECHODIS_ADDRESS);
        }
        return new byte[0];
    }
}