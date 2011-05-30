package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

public class ModuleType extends AbstractRadioCommand {

    ModuleType(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private static final double MAX = 0x20;
    private int type;
    private int equipment_type;
    private int rssiLevel;

    public int getType() {
        return type;
    }

    public int getEquipment_type() {
        return equipment_type;
    }

    /*
    A percentage representing the saturation. 100% = full saturation = 48 dB
     */
    public double getRssiLevel() {
        return (((double) rssiLevel) / MAX) * 100;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ModuleType;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        type = data[0] & 0xFF;
        rssiLevel = data[1] & 0xFF;
        equipment_type = data[3] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }
}