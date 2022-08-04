package com.energyict.dlms.axrdencoding;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.math.BigDecimal;

public class CosemTime extends AbstractDataType {

    int size = 5; // fixed

    int hour;
    int minutes;
    int seconds;
    int hundreths;

    public CosemTime(byte[] berEncodedData, int offset) throws ProtocolException {
        int workingOffset = offset;
        if (berEncodedData[workingOffset] != AxdrType.TIME27.getTag()) {
            throw new ProtocolException("Time, invalid identifier " + berEncodedData[workingOffset]);
        }
        workingOffset++;
        hour = ProtocolUtils.getByte2Int(berEncodedData, workingOffset);
        workingOffset++;
        minutes = ProtocolUtils.getByte2Int(berEncodedData, workingOffset);
        workingOffset++;
        seconds = ProtocolUtils.getByte2Int(berEncodedData, workingOffset);
        workingOffset++;
        hundreths = ProtocolUtils.getByte2Int(berEncodedData, workingOffset);
    }

    @Override
    protected byte[] doGetBEREncodedByteArray() {
        return new byte[] {
                        AxdrType.TIME27.getTag(),
                        (byte) (hour),
                        (byte) (minutes),
                        (byte) (seconds),
                        (byte) (hundreths),
                };
    }

    @Override
    protected int size() {
        return size;
    }

    @Override
    public int intValue() {
        return -1;
    }

    @Override
    public BigDecimal toBigDecimal() {
        return null;
    }

    @Override
    public long longValue() {
        return -1;
    }

    @Override
    public String toString() {
        String hourString;
        String minutesString;
        String secondsString;
        String hundrethsString;
        if (hourIsSet()) {
            hourString = format2digits(hour);
        } else {
            hourString = "Every hour at";
        }
        if (minutesIsSet()) {
            minutesString = format2digits(minutes);
        } else {
            minutesString = "Every minute at";
        }
        if (secondsIsSet()) {
            secondsString = format2digits(seconds);
        } else {
            secondsString = "Every second at";
        }
        if (hundrethsIsSet()) {
            hundrethsString = format2digits(hundreths);
        } else {
            hundrethsString = "not set";
        }
        if (!hourIsSet() && !minutesIsSet()) {
            return String.join(":", minutesString, secondsString, hundrethsString);
        }
        return String.join(":", hourString, minutesString, secondsString, hundrethsString);
    }

    @Override
    public OctetString getOctetString() {
        byte[] bytes = {
                (byte) (hour),
                (byte) (minutes),
                (byte) (seconds),
                (byte) (hundreths)
        };
        return OctetString.fromByteArray(bytes, bytes.length);
    }

    private String format2digits(int value) {
        return String.format("%02d", value);
    }

    private boolean hundrethsIsSet() {
        return hundreths >= 0 && hundreths <= 99;
    }

    private boolean secondsIsSet() {
        return seconds >= 0 && seconds < 60;
    }

    private boolean minutesIsSet() {
        return minutes >= 0 && minutes < 60;
    }

    private boolean hourIsSet() {
        return hour >= 0 && hour < 24;
    }

}
