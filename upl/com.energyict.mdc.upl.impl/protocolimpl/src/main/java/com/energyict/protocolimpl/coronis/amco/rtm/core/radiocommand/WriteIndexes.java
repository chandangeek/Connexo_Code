package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class WriteIndexes extends AbstractRadioCommand {

    WriteIndexes(RTM rtm, int input) {
        super(propertySpecService, rtm);
        writingType = (int) Math.pow(2, input - 1);             //A = 1, B = 2, C = 4, D = 8
    }

    private int indexA = 0;
    private int indexB = 0;
    private int indexC = 0;
    private int indexD = 0;

    private int writingType = 0x01;

    public void setIndex(int value) {
        if (writingType == 0x01) {
            setIndexA(value);
        }
        if (writingType == 0x02) {
            setIndexB(value);
        }
        if (writingType == 0x04) {
            setIndexC(value);
        }
        if (writingType == 0x08) {
            setIndexD(value);
        }
    }

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
    protected void parse(byte[] data) throws IOException {
        if ((data[0] & 0xFF) == 0xFF) {
            throw new WaveFlowException("Error writing the index");
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        byte[] bytesA = ProtocolTools.getBytesFromInt(indexA, 4);
        byte[] bytesB = ProtocolTools.getBytesFromInt(indexB, 4);
        byte[] bytesC = ProtocolTools.getBytesFromInt(indexC, 4);
        byte[] bytesD = ProtocolTools.getBytesFromInt(indexD, 4);
        return ProtocolTools.concatByteArrays(new byte[]{(byte) writingType}, bytesA, bytesB, bytesC, bytesD);
    }
}