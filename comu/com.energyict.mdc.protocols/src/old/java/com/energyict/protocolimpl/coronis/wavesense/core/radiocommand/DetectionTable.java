package com.energyict.protocolimpl.coronis.wavesense.core.radiocommand;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.wavesense.WaveSense;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;

public class DetectionTable extends AbstractRadioCommand {


    public DetectionTable(WaveSense waveSense) {
        super(waveSense);
    }

    private static final double VOLTAGE_MULTIPLIER = (1 / 819);
    private static final double AMPERE_MULTIPLIER = (1 / 256);
    private static final int AMPERE_OFFSET = 4;
    private static final double AMPERE_MIN_VALUE = 4;
    private static final double AMPERE_MAX_VALUE = 20;

    private ThresholdEvent[] lowThresholdEvents = new ThresholdEvent[5];
    private ThresholdEvent[] highThresholdEvents = new ThresholdEvent[5];

    public ThresholdEvent[] getHighThresholdEvents() {
        return highThresholdEvents;
    }

    public ThresholdEvent[] getLowThresholdEvents() {
        return lowThresholdEvents;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.DetectionTable;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        byte status;
        Date eventDate;
        int duration;
        double integratedValue;
        int offset = 0;

        for (int i = 0; i < 5; i++) {
            status = data[offset];
            offset++;
            eventDate = TimeDateRTCParser.parse(data, offset, 6, getWaveSense().getTimeZone()).getTime();
            offset += 6;
            duration = ProtocolTools.getUnsignedIntFromBytes(data, offset, 2) * getWaveSense().getProfileInterval();
            offset += 2;
            integratedValue = ProtocolTools.getUnsignedIntFromBytes(data, offset, 2);
            offset += 2;
            highThresholdEvents[i] = new ThresholdEvent(duration, eventDate, integratedValue, status);
        }

        for (int i = 0; i < 5; i++) {
            status = data[offset];
            offset++;
            eventDate = TimeDateRTCParser.parse(data, offset, 6, getWaveSense().getTimeZone()).getTime();
            offset += 6;
            duration = ProtocolTools.getUnsignedIntFromBytes(data, offset, 2) * getWaveSense().getProfileInterval();
            offset += 2;
            integratedValue = calcValue(ProtocolTools.getUnsignedIntFromBytes(data, offset, 2));
            offset += 2;
            lowThresholdEvents[i] = new ThresholdEvent(duration, eventDate, integratedValue, status);
        }
    }

    private double calcValue(int rawValue) throws IOException {
        double realValue = 0;
        if (getWaveSense().getRadioCommandFactory().readModuleType().isOfType05Voltage()) {
            realValue = rawValue * VOLTAGE_MULTIPLIER;
        } else if (getWaveSense().getRadioCommandFactory().readModuleType().isOfType420MilliAmpere()) {
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

    protected byte[] prepare() throws IOException {
        return new byte[0];         //No extra bytes needed for a detection table request.
    }


}