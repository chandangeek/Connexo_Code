package com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public class WriteIndexes extends AbstractRadioCommand {

    public WriteIndexes(WaveFlow waveFlow) {
        super(waveFlow);
        this.inputsUsed = waveFlow.getParameterFactory().readOperatingMode().getNumberOfInputsUsed();
        applyWritingMode();
    }

    public WriteIndexes(WaveFlow waveFlow, int input) {
        super(waveFlow);
        this.inputsUsed = waveFlow.getParameterFactory().readOperatingMode().getNumberOfInputsUsed();
        writingType = (int) Math.pow(2, input - 1);
    }

    private void applyWritingMode() {
        if (inputsUsed < 4) {
            this.setWritingType(0x03);          //0000.0011 = update only index A and B
        }
        if (inputsUsed == 4) {
            this.setWritingType(0x0F);          //0000.1111 = update index A, B, C and D
        }
    }

    private int indexA = 0;
    private int indexB = 0;
    private int indexC = 0;
    private int indexD = 0;
    private int inputsUsed;

    private int writingType = 0x03;

    public void setIndexA(int indexA) {
        this.indexA = indexA;
    }

    public void setIndexB(int indexB) {
        this.indexB = indexB;
    }

    public void setIndexC(int indexC) {
        this.indexC = indexC;
    }

    public void setIndexD(int indexD) {
        this.indexD = indexD;
    }

    public void setWritingType(int writingType) {
        this.writingType = writingType;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.WriteIndexes;
    }

    @Override
    public void parse(byte[] data) {
        if ((data[0] & 0xFF) == 0xFF) {
            throw createWaveFlowException("Error writing the index");
        }
    }

    @Override
    protected byte[] prepare() {
        byte[] bytesA = ProtocolTools.getBytesFromInt(indexA, 4);
        byte[] bytesB = ProtocolTools.getBytesFromInt(indexB, 4);

        if (writingType < 4) {
            return ProtocolTools.concatByteArrays(new byte[]{(byte) writingType}, bytesA, bytesB);
        } else {
            byte[] bytesC = ProtocolTools.getBytesFromInt(indexC, 4);
            byte[] bytesD = ProtocolTools.getBytesFromInt(indexD, 4);
            return ProtocolTools.concatByteArrays(new byte[]{(byte) writingType}, bytesA, bytesB, bytesC, bytesD);
        }
    }
}