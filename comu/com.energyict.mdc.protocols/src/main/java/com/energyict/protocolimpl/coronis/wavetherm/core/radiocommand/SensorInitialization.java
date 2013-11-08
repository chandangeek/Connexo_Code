package com.energyict.protocolimpl.coronis.wavetherm.core.radiocommand;

import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 28-mrt-2011
 * Time: 9:39:09
 */
public class SensorInitialization extends AbstractRadioCommand {

    protected SensorInitialization(WaveTherm waveTherm) {
        super(waveTherm);
    }

    private int numberOfSensors;
    private SensorId id1 = null;
    private SensorId id2 = null;

    @Override
    protected void parse(byte[] data) throws IOException {
        int offset = 0;

        numberOfSensors = data[offset] & 0xFF;
        offset++;

        int familyCode;
        byte[] serialNumber;

        if (numberOfSensors > 0) {
            familyCode = data[offset] & 0xFF;
            offset++;
            serialNumber = ProtocolTools.getSubArray(data, offset, 6);
            offset += 6;
            offset++; //Skip the crc byte
            id1 = new SensorId(familyCode, serialNumber);
        }

        if (numberOfSensors > 1) {
            familyCode = data[offset] & 0xFF;
            offset++;
            serialNumber = ProtocolTools.getSubArray(data, offset, 6);
            offset += 6;
            offset++;
            id2 = new SensorId(familyCode, serialNumber);
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.SensorInitialization;
    }
}