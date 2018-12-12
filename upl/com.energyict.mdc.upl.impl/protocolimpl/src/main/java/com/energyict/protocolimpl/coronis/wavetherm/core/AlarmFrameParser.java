package com.energyict.protocolimpl.coronis.wavetherm.core;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;
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

    private WaveTherm waveTherm;
    private int status;
    private Date date;
    private int duration;
    private double integratedValue;
    private int sensorNumber;
    private boolean validIntegratedValue = true;

    public AlarmFrameParser(WaveTherm waveTherm) {
        this.waveTherm = waveTherm;
    }

    public Date getDate() {
        return date;
    }

    public int getStatus() {
        return status;
    }

    public void parse(byte[] data) throws IOException {
        int offset = 1;

        status = data[offset] & 0xFF;
        offset++;

        date = TimeDateRTCParser.parse(data, offset, 6, waveTherm.getTimeZone()).getTime();
        offset += 6;

        if (data.length > 7) {
            sensorNumber = data[offset] & 0xFF;
            offset++;

            duration = ProtocolTools.getUnsignedIntFromBytes(data, offset, 2);
            offset += 2;

            int rawValue = ProtocolTools.getUnsignedIntFromBytes(data, offset, 2);
            if (rawValue == 0x4FFF) {
                validIntegratedValue = false;
            }

            integratedValue = calcValue(rawValue);
            offset += 2;
        }
    }

    private double calcValue(int rawValue) {
        double sign = ((rawValue & 0xF800) == 0xF800) ? -1 : 1;  //b15 b14 b12 b11 b10 = 11111 ? ==> indicates a negative value
        return sign * (rawValue & 0x07FF) / 16;
    }

    public List<MeterEvent> getMeterEvents() throws IOException {
        List<MeterEvent> events = new ArrayList<>();

        if ((status & 0x01) == 0x01) {
            String integratedValue = validIntegratedValue ? this.integratedValue + " \u00B0C" : "missing.";
            events.add(new MeterEvent(date, 0, "Low threshold detection.  Sensor number: " + sensorNumber + ". Duration: " + duration + ". Integrated value: " + integratedValue));
        }
        if ((status & 0x02) == 0x02) {
            String integratedValue = validIntegratedValue ? this.integratedValue + " \u00B0C" : "missing.";
            events.add(new MeterEvent(date, 0, "High threshold detection. Sensor number: " + sensorNumber + ". Duration: " + duration + ". Integrated value: " + integratedValue));
        }
        if ((status & 0x04) == 0x04) {
            events.add(new MeterEvent(date, 0, "End of battery life detection."));
        }

        return events;
    }

    /**
     * Used in the acknowledgement of the push frame.
     */
    public byte[] getResponse() {
        return new byte[]{(byte) 0xC0, (byte) status};
    }
}