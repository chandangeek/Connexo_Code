/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavesense.core.radiocommand;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;

import com.energyict.protocolimpl.coronis.wavesense.WaveSense;

import java.io.IOException;

public class ModuleType extends AbstractRadioCommand {

    private static final String VOLT_DESCRIPTION = "volt";
    private static final String MILLIAMPERE_DESCRIPTION = "milliampere";


    public ModuleType(WaveSense waveSense) {
        super(waveSense);
    }

    public ModuleType(WaveSense waveSense, int type) {
        super(waveSense);
        this.type = type;
    }

    public static final int WAVESENSE_0_5_V = 0x22;
    public static final int WAVESENSE_4_20_MA = 0x23;
    public static final String WAVESENSE_0_5_V_DESCRIPTION = "Wavesense 0-5 V";
    public static final String WAVESENSE_4_20_MA_DESCRIPTION = "Wavesense 4-20 mA";
    private static final String WAVESENSE_DEFAULT_DESCRIPTION = "Wavensense module";

    private int type;
    private int equipmentType;
    private int rssiLevel;
    private int currentAwakeningPeriod;

    public int getType() {
        return type;
    }

    public String getDescription() {
        if (isOfType05Voltage()) {
            return WAVESENSE_0_5_V_DESCRIPTION;
        }
        if (isOfType420MilliAmpere()) {
            return WAVESENSE_4_20_MA_DESCRIPTION;
        }
        return WAVESENSE_DEFAULT_DESCRIPTION;
    }

    public int getEquipmentType() {
        return equipmentType;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ModuleType;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        type = data[offset++];
        rssiLevel = data[offset++];
        currentAwakeningPeriod = data[offset++];
        equipmentType = data[offset] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];        //Write empty byte string, aka no additional preparation necessary
    }

    public boolean isOfType05Voltage() {
        return (getType() == ModuleType.WAVESENSE_0_5_V);
    }

    public boolean isOfType420MilliAmpere() {
        return (getType() == ModuleType.WAVESENSE_4_20_MA);
    }

    public Unit getUnit() {
        if (isOfType05Voltage()) {
            return Unit.get(BaseUnit.VOLT);
        } else if (isOfType420MilliAmpere()) {
            return Unit.get(BaseUnit.AMPERE, -3);
        } else {
            return Unit.get("");
        }
    }

    public String getUnitDescription() {
        if (isOfType05Voltage()) {
            return VOLT_DESCRIPTION;
        } else if (isOfType420MilliAmpere()) {
            return MILLIAMPERE_DESCRIPTION;
        }
        return "";
    }
}