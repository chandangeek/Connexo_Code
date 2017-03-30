/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.PulseWeight;
import com.energyict.protocolimpl.utils.ProtocolTools;

public class GenericHeader {

    private WaveFlow waveFlow;

    public GenericHeader(WaveFlow waveFlow) {
        this.waveFlow = waveFlow;
    }

    /**
     * All meta data included in this header is parsed and then cached in the factories.
     *
     * @param data bytes representing the header
     */
    public void parse(byte[] data) {
        int extendedOperationMode = data[1] & 0xFF;
        waveFlow.getParameterFactory().setExtendedOperationMode(extendedOperationMode);

        int operationMode = data[2] & 0xFF;
        waveFlow.getParameterFactory().setOperatingMode(operationMode);

        int applicationStatus = data[3] & 0xFF;
        waveFlow.getParameterFactory().setApplicationStatus(applicationStatus);

        int qos = ProtocolTools.getUnsignedIntFromBytes(data, 12, 1);
        waveFlow.getRadioCommandFactory().setModuleType(new ModuleType(waveFlow, qos));

        int shortLifeCounter = ProtocolTools.getUnsignedIntFromBytes(data, 13, 2);
        waveFlow.getParameterFactory().setBatteryLifeDurationCounter(shortLifeCounter);

        PulseWeight pulseA = new PulseWeight(waveFlow, 1);
        PulseWeight pulseB = new PulseWeight(waveFlow, 2);
        PulseWeight pulseC = new PulseWeight(waveFlow, 3);
        PulseWeight pulseD = new PulseWeight(waveFlow, 4);
        pulseA.parse(new byte[]{data[15]});
        pulseB.parse(new byte[]{data[16]});
        pulseC.parse(new byte[]{data[17]});
        pulseD.parse(new byte[]{data[18]});

        waveFlow.setPulseWeights(new PulseWeight[]{pulseA, pulseB, pulseC, pulseD});
    }
}