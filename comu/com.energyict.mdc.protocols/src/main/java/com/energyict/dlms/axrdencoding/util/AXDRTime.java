package com.energyict.dlms.axrdencoding.util;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;

public final class AXDRTime extends AbstractDataType {

    private static final int SIZE = 0x04;
    private static final int NR_MILLIS_IN_HUNDREDTH_OF_SECOND = 10;

    private int hour;
    private int minutes;
    private int seconds;
    private int milliSeconds;

    public AXDRTime() {
    }

    public AXDRTime(OctetString octetString) {
        this(octetString.getBEREncodedByteArray());
    }

    public AXDRTime(byte[] berEncodedData) {
        this(berEncodedData, 0);
    }

    public AXDRTime(byte[] berEncodedData, int offset) {
        int ptr = offset;

        ptr = ptr + 2;

        int hour = ProtocolUtils.getByte2Int(berEncodedData, ptr);
        this.hour = hour != 0xFF ? hour : 0;
        ptr = ptr + 1;

        final int minutes = ProtocolUtils.getByte2Int(berEncodedData, ptr);
        this.minutes = minutes != 0xFF ? minutes : 0;
        ptr = ptr + 1;

        final int seconds = ProtocolUtils.getByte2Int(berEncodedData, ptr);
        this.seconds = seconds != 0xFF ? seconds : 0;
        ptr = ptr + 1;

        final int miliisSeconds = ProtocolUtils.getByte2Int(berEncodedData, ptr) * NR_MILLIS_IN_HUNDREDTH_OF_SECOND;
        milliSeconds = miliisSeconds != 0xFF0 ? miliisSeconds : 0;
        ptr = ptr + 1;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getMilliSeconds() {
        return milliSeconds;
    }

    public void setMilliSeconds(int milliSeconds) {
        this.milliSeconds = milliSeconds;
    }

    /**
     * Set the time
     * @param time String containing the time in format "hh:mm:ss"
     */
    public void setTime(String time) throws IOException {
        try {
            final String[] split = time.split(":");
            hour = Integer.parseInt(split[0]);
            minutes = Integer.parseInt(split[1]);
            seconds = Integer.parseInt(split[2]);
            milliSeconds = 0;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("AXDRTime: Failed to parse the given time (" + time + "); Time should be in format 'hh:mm:ss'");
        } catch (NumberFormatException e) {
            throw new IOException("AXDRTime: Failed to parse the given time (" + time + "); Time should be in format 'hh:mm:ss'");
        }
    }

    public String getTime() {
        String readableTime = String.format("%02d", hour) + "h:" + String.format("%02d", minutes) + "m:" + String.format("%02d", seconds) + "s";
        if (milliSeconds != 0) {
            readableTime += ":";
            readableTime += String.format("%03d", milliSeconds);
            readableTime += "ms";
        }
        return readableTime;
    }

    public String getShortTimeDescription() {
        return String.format("%02d", hour) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
    }

    @Override
    public OctetString getOctetString() {
        byte[] bytes = {
                (byte) (hour),
                (byte) (minutes),
                (byte) (seconds),
                (byte) (milliSeconds / NR_MILLIS_IN_HUNDREDTH_OF_SECOND)
        };
        return OctetString.fromByteArray(bytes, bytes.length);
    }

    @Override
    protected byte[] doGetBEREncodedByteArray() {
        return
                new byte[]{
                        AxdrType.OCTET_STRING.getTag(),
                        (byte) SIZE,
                        (byte) (hour),
                        (byte) (minutes),
                        (byte) (seconds),
                        (byte) (milliSeconds / NR_MILLIS_IN_HUNDREDTH_OF_SECOND)
                };
    }

    @Override
    protected int size() {
        return SIZE;
    }

    @Override
    public int intValue() {
        return -1;
    }

    @Override
    public long longValue() {
        return -1;
    }

    @Override
    public BigDecimal toBigDecimal() {
        return getOctetString().toBigDecimal();
    }
}
