/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavesense.core.radiocommand;

import com.energyict.protocolimpl.coronis.wavesense.WaveSense;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class CurrentReading extends AbstractRadioCommand {

    private static final double VOLTAGE_MULTIPLIER = (1 / 819);
    private static final double AMPERE_MULTIPLIER = (1 / 256);
    private static final int AMPERE_OFFSET = 4;
    private static final double AMPERE_MIN_VALUE = 4;
    private static final double AMPERE_MAX_VALUE = 20;

    public CurrentReading(WaveSense waveSense) {
        super(waveSense);
    }

    private double currentValue;
    private int operationMode;
    private int applicationStatus;

    public final double getCurrentValue() {
        return currentValue;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ReadCurrentValue;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        operationMode = data[offset++];
        applicationStatus = data[offset++];
        if (getWaveSense().getRadioCommandFactory().readModuleType().isOfType05Voltage()) {
            currentValue = ProtocolTools.getUnsignedIntFromBytes(data, offset, 2) * VOLTAGE_MULTIPLIER;
        } else if (getWaveSense().getRadioCommandFactory().readModuleType().isOfType420MilliAmpere()) {
            int value = ProtocolTools.getUnsignedIntFromBytes(data, offset, 2);
            if (value == 0xEEEE) {
                currentValue = AMPERE_MIN_VALUE;
            } else if (value == 0xFFFF) {
                currentValue = AMPERE_MAX_VALUE;
            } else {
                currentValue = (value * AMPERE_MULTIPLIER) + AMPERE_OFFSET;
            }
        }
    }

    protected byte[] prepare() throws IOException {
        return new byte[0];
    }
}