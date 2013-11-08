package com.energyict.protocolimpl.coronis.wavetherm.core.radiocommand;

import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class CurrentReading extends AbstractRadioCommand {

    public CurrentReading(WaveTherm waveTherm) {
        super(waveTherm);
    }

    private double currentValueA;
    private double currentValueB;
    private int operationMode;
    private int applicationStatus;
    private boolean validA = true;
    private boolean validB = true;

    public double getCurrentValueA() {
        return currentValueA;
    }

    public double getCurrentValueB() {
        return currentValueB;
    }

    public double[] getCurrentValues() {
        return new double[]{getCurrentValueA(), getCurrentValueB()};
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


        int rawValueA = ProtocolTools.getUnsignedIntFromBytes(data, offset, 2);
        if (rawValueA == 0x4FFF) {
            validA = false;
        }
        currentValueA = calcValue(rawValueA);
        offset +=2;

        int rawValueB = ProtocolTools.getUnsignedIntFromBytes(data, offset, 2);
        if (rawValueB == 0x4FFF) {
            validB = false;
        }
        currentValueB = calcValue(rawValueB);
    }

    private double calcValue(int rawValue) {
        double sign = ((rawValue & 0xF800) == 0xF800) ? -1 : 1;  //b15 b14 b12 b11 b10 = 11111 ? ==> indicates a negative value
        return sign * (rawValue & 0x07FF) / 16;
    }

    protected byte[] prepare() throws IOException {
        return new byte[0];
    }
}