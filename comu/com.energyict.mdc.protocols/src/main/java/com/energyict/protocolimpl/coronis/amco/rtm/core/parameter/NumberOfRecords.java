package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 15-apr-2011
 * Time: 9:23:34
 */
public class NumberOfRecords extends AbstractParameter {

    NumberOfRecords(RTM rtm) {
        super(rtm);
    }

    private int number;

    public int getNumber() {
        return number;
    }

    @Override
    ParameterId getParameterId() throws WaveFlowException {
        return ParameterId.NumberOfRecords;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        number = ProtocolTools.getUnsignedIntFromBytes(data, 0, 2);
    }

    @Override
    protected byte[] prepare() throws IOException {
        throw new UnsupportedException("Not allowed to write this parameter");
    }
}
