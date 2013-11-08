package com.energyict.protocolimpl.coronis.waveflow.hydreka.parameter;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.ModuleType;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 21/10/11
 * Time: 16:11
 */
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

        int qos = ProtocolTools.getUnsignedIntFromBytes(data, 12, 1);
        waveFlow.getRadioCommandFactory().setModuleType(new ModuleType(waveFlow, qos));

        int shortLifeCounter = ProtocolTools.getUnsignedIntFromBytes(data, 13, 2);
        waveFlow.getParameterFactory().setBatteryLifeDurationCounter(shortLifeCounter);
    }
}