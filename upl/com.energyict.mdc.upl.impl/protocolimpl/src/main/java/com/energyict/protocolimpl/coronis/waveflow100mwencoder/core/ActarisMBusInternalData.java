package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.coronis.waveflow.core.EventStatusAndDescription;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class ActarisMBusInternalData extends InternalData {

    static public final int MBUS_INTERNAL_DATA_LENGTH = 74;


    /**
     * raw string of the internal data
     */
    private byte[] encoderInternalData;
    private String serialNumber = "";

    /**
     * 0x0001 Not used
     * 0x0002 Not used
     * 0x0004 Not used
     * 0x0008 Not used
     * 0x0010 ERR_EXT_BACKFLOW
     * 0x0020 ERR_EXT_AIR_IN_PIPE
     * 0x0040 ERR_EXT_OVERFLOW
     * 0x0080 ERR_EXT_US_ASIC
     * 0x0100 ERR_EXT_DIRTY_TRANS
     * 0x0200 ERR_EXT_VERY_DIRTY_TRANS
     * 0x0400 ERR_EXT_MICRO_COM
     * 0x0800 ERR_EXT_FLOWMETER_TAMPER
     * 0x1000 Not used
     * 0x2000 Not used
     * 0x4000 ERR_EXT_MODEM_PSTN
     */
    private int alarmCode;

    final public byte[] getEncoderInternalData() {
        return encoderInternalData;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public int getAlarmCode() {
        return alarmCode;
    }

    public List<MeterEvent> getMeterEvents() {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        if ((alarmCode & 0x0010) == 0x0010) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, portId == 0 ? EventStatusAndDescription.EVENTCODE_SIMPLE_BACKFLOW_A : EventStatusAndDescription.EVENTCODE_SIMPLE_BACKFLOW_B, "Backflow in meter or in pipe system " + getPortInfo()));
        }
        if ((alarmCode & 0x0020) == 0x0020) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, portId == 0 ? EventStatusAndDescription.EVENTCODE_AIR_IN_PIPE_A : EventStatusAndDescription.EVENTCODE_AIR_IN_PIPE_B, "Air in the pipe system, broken ultrasound probe or very strong deposits on the probes" + getPortInfo()));
        }
        if ((alarmCode & 0x0040) == 0x0040) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, portId == 0 ? EventStatusAndDescription.EVENTCODE_OVERFLOW_A : EventStatusAndDescription.EVENTCODE_OVERFLOW_B, "Exceeding the maximum admissible flow " + getPortInfo()));
        }
        if ((alarmCode & 0x0080) == 0x0080) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, portId == 0 ? EventStatusAndDescription.EVENTCODE_US_ASIC_A : EventStatusAndDescription.EVENTCODE_US_ASIC_B, "US_ASIC" + getPortInfo()));
        }
        if ((alarmCode & 0x0100) == 0x0100) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, portId == 0 ? EventStatusAndDescription.EVENTCODE_DIRTY_TRANS_A : EventStatusAndDescription.EVENTCODE_DIRTY_TRANS_B, "Dirty trans" + getPortInfo()));
        }
        if ((alarmCode & 0x0200) == 0x0200) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, portId == 0 ? EventStatusAndDescription.EVENTCODE_VERY_DIRTY_TRANS_A : EventStatusAndDescription.EVENTCODE_VERY_DIRTY_TRANS_B, "Very dirty trans" + getPortInfo()));
        }
        if ((alarmCode & 0x0400) == 0x0400) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, portId == 0 ? EventStatusAndDescription.EVENTCODE_MICRO_COM_A : EventStatusAndDescription.EVENTCODE_MICRO_COM_B, "Connection cable with flow transponder or connection to the ultrasound probes has been interrupted" + getPortInfo()));
        }
        if ((alarmCode & 0x0800) == 0x0800) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, portId == 0 ? EventStatusAndDescription.EVENTCODE_FLOWMETER_TAMPER_A : EventStatusAndDescription.EVENTCODE_FLOWMETER_TAMPER_B, "Flowmeter tamper" + getPortInfo()));
        }
        if ((alarmCode & 0x4000) == 0x4000) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, portId == 0 ? EventStatusAndDescription.EVENTCODE_MODEM_PSTN_A : EventStatusAndDescription.EVENTCODE_MODEM_PSTN_B, "Modem PSTN" + getPortInfo()));
        }
        return meterEvents;
    }

    ActarisMBusInternalData(final byte[] data, final Logger logger, int portId) throws IOException {
        this.portId = portId;

        if (data.length != MBUS_INTERNAL_DATA_LENGTH) {
            throw new WaveFlow100mwEncoderException("Invalid encoder internal data length. Expected length [" + MBUS_INTERNAL_DATA_LENGTH + "], received length [" + data.length + "]");
        }

        encoderInternalData = data;

        byte[] serialNumberBytes = ProtocolTools.reverseByteArray(ProtocolTools.getSubArray(data, 17, 21));
        int serialNumberValue = ProtocolTools.getUnsignedIntFromBytes(serialNumberBytes);
        serialNumber = ProtocolTools.getHexStringFromInt(serialNumberValue, 4, "");
        byte[] alarmCodeBytes = ProtocolTools.getReverseByteArray(ProtocolTools.getSubArray(data, 70, 72));
        alarmCode = ProtocolTools.getIntFromBytes(alarmCodeBytes);

        DataInputStream dais = null;
        try {
            byte[] temp;
            char c;
            int i;

            dais = new DataInputStream(new ByteArrayInputStream(data));

        } finally {
            if (dais != null) {
                try {
                    dais.close();
                } catch (IOException e) {
                    logger.severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
                }
            }
        }
    }
}
