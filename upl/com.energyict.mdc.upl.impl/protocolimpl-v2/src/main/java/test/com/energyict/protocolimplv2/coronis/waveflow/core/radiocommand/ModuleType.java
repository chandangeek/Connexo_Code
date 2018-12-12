package test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand;

import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public class ModuleType extends AbstractRadioCommand {

    ModuleType(WaveFlow waveFlow) {
        super(waveFlow);
    }

    public ModuleType(WaveFlow waveFlow, int rssiLevel) {
        super(waveFlow);
        this.rssiLevel = rssiLevel;
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
        double value = (((double) rssiLevel) / MAX) * 100;
        return Math.round(value * 100.0) / 100.0;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ModuleType;
    }

    @Override
    protected void parse(byte[] data) {
        type = data[0] & 0xFF;
        rssiLevel = data[1] & 0xFF;
        equipment_type = data[3] & 0xFF;
    }

    @Override
    protected byte[] prepare() {
        return new byte[0];
    }
}