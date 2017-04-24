/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class CurrentRegisterReading extends AbstractRadioCommand {

    public CurrentRegisterReading(RTM rtm) {
        super(rtm);
    }

    private int currentReadingA;
    private int currentReadingB;
    private int currentReadingC = 0;
    private int currentReadingD = 0;
    private int numberOfPorts;

    public int getNumberOfPorts() {
        return numberOfPorts;
    }

    public int getCurrentReadingA() {
        return currentReadingA;
    }

    public int getCurrentReadingB() {
        return currentReadingB;
    }

    public int getCurrentReadingC() {
        return currentReadingC;
    }

    public int getCurrentReadingD() {
        return currentReadingD;
    }

    @Override
    public void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        parse(data, null, rtmFactory);
    }

    public void parse(byte[] data, byte[] radioAddress, RTMFactory rtmFactory) throws IOException {

        getGenericHeader().setRadioAddress(radioAddress);
        getGenericHeader().parse(data, rtmFactory);
        numberOfPorts = getGenericHeader().getOperationMode().readNumberOfPorts();
        int offset = 23; //Skip the generic header

        if (data.length == 23) {
            return;     //In case of the evoHop profile, no readings were sent.
        }

        currentReadingA = ProtocolTools.getIntFromBytes(data, offset, 4);
        offset += 4;
        currentReadingB = ProtocolTools.getIntFromBytes(data, offset, 4);
        offset += 4;

        if (data.length > 31) {
            currentReadingC = ProtocolTools.getIntFromBytes(data, offset, 4);
            offset += 4;
        }
        if (data.length > 35) {
            currentReadingD = ProtocolTools.getIntFromBytes(data, offset, 4);
            offset += 4;
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.CurrentRegisterReading;
    }

    public int getCurrentReading(int port) throws WaveFlowException {
        if (port == 1) {
            return getCurrentReadingA();
        }
        if (port == 2) {
            return getCurrentReadingB();
        }
        if (port == 3) {
            return getCurrentReadingC();
        }
        if (port == 4) {
            return getCurrentReadingD();
        }
        throw new WaveFlowException("Port number " + port + "is not supported by the meter");
    }
}