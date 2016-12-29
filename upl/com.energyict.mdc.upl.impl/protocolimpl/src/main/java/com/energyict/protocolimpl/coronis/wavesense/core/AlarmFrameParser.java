package com.energyict.protocolimpl.coronis.wavesense.core;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.wavesense.WaveSense;
import com.energyict.protocolimpl.coronis.wavesense.core.radiocommand.ModuleType;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 25-mrt-2011
 * Time: 16:27:08
 */
public class AlarmFrameParser {

    private static final double VOLTAGE_MULTIPLIER = (1 / 819);
    private static final double AMPERE_MULTIPLIER = (1 / 256);
    private static final int AMPERE_OFFSET = 4;
    private static final double AMPERE_MIN_VALUE = 4;
    private static final double AMPERE_MAX_VALUE = 20;

    private WaveSense waveSense;
    private int status;
    private Date date;
    private int duration;
    private double integratedValue;

    public AlarmFrameParser(WaveSense waveSense) {
        this.waveSense = waveSense;
    }

    public Date getDate() {
        return date;
    }

    public int getStatus() {
        return status;
    }

    public void parse(byte[] data) throws IOException {
        int offset = 1;      //Skip the alarm id

        status = data[offset] & 0xFF;
        offset++;

        date = TimeDateRTCParser.parse(data, offset, 6, waveSense.getTimeZone()).getTime();
        offset += 6;

        if (data.length > 7) {
            int sensornumber = data[offset] & 0xFF;
            offset++;

            duration = ProtocolTools.getUnsignedIntFromBytes(data, offset, 2);
            offset += 2;

            integratedValue = calcValue(ProtocolTools.getUnsignedIntFromBytes(data, offset, 2));
            offset += 2;
        }
    }

    private double calcValue(int rawValue) throws IOException {
        double realValue = 0;
        ModuleType moduleType = waveSense.getRadioCommandFactory().readModuleType();

        if (moduleType.isOfType05Voltage()) {
            realValue = rawValue * VOLTAGE_MULTIPLIER;
        } else if (moduleType.isOfType420MilliAmpere()) {
            if (rawValue == 0xEEEE) {
                realValue = AMPERE_MIN_VALUE;
            } else if (rawValue == 0xFFFF) {
                realValue = AMPERE_MAX_VALUE;
            } else {
                realValue = (rawValue * AMPERE_MULTIPLIER) + AMPERE_OFFSET;
            }
        }
        return realValue;
    }


    public List<MeterEvent> getMeterEvents() throws IOException {
        List<MeterEvent> events = new ArrayList<>();

        if ((status & 0x01) == 0x01) {
            events.add(new MeterEvent(date, 0, "Low threshold detection. Duration: " + duration + " (in multiples of detection measurement period. Integrated value: " + integratedValue));
        }
        if ((status & 0x02) == 0x02) {
            events.add(new MeterEvent(date, 0, "High threshold detection. Duration: " + duration + " (in multiples of detection measurement period. Integrated value: " + integratedValue));
        }
        if ((status & 0x04) == 0x04) {
            events.add(new MeterEvent(date, 0, "End of battery life detection"));
        }
        if ((status & 0x08) == 0x08) {
            events.add(new MeterEvent(date, 0, "Sensor fault detection"));
        }

        return events;
    }

    /**
     * Used in the acknowledgement of the push frame.
     */
    public byte[] getResponseACK() {
        return new byte[]{(byte) 0xC0, (byte) status};
    }

}
