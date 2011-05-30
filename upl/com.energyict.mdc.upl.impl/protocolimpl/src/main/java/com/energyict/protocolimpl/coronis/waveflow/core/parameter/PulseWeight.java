package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

public class PulseWeight extends AbstractParameter {

    private int inputChannel;
    private int weight;
    private int unitScaler;

    public PulseWeight(WaveFlow waveFlow, int inputChannel) {
        super(waveFlow);
        this.inputChannel = inputChannel;
    }

    @Override
    ParameterId getParameterId() {
        switch (inputChannel) {
            case 1:
                return ParameterId.DefinePulseWeightA;
            case 2:
                return ParameterId.DefinePulseWeightB;
            case 3:
                return ParameterId.DefinePulseWeightC;
            case 4:
                return ParameterId.DefinePulseWeightD;
            default:
                return ParameterId.DefinePulseWeightA;
        }
    }

    public int getInputChannel() {
        return inputChannel;
    }

    public void setInputChannel(int inputChannel) {
        this.inputChannel = inputChannel;
    }

    public int getUnitScaler() {
        return unitScaler;
    }

    public void setUnitScaler(int unitScaler) {
        this.unitScaler = unitScaler;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        weight = data[0] & 0x0F;
        unitScaler = (data[0] >> 4) - 3;
    }

    @Override
    protected byte[] prepare() throws IOException {
        int prepare = weight;
        prepare = prepare | ((unitScaler + 3) << 4);
        return new byte[]{(byte) prepare};
    }

    public Unit getUnit() {
        return Unit.get(BaseUnit.LITER, getUnitScaler());
    }
}