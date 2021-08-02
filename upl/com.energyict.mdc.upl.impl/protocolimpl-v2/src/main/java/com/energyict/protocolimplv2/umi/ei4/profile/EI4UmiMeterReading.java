package com.energyict.protocolimplv2.umi.ei4.profile;

import com.energyict.protocolimplv2.umi.ei4.structures.UmiHelper;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class EI4UmiMeterReading extends LittleEndianData {
    public static final int SIZE = 12;

    /**
     * Gas volume measured in the current interval
     */
    private final long reading; // 4 bytes
    /**
     * Corresponding timestamp of 'meterReading'
     */
    private final Date timestamp;    // 4 bytes
    /**
     * Bit 0: There was a clock sync (ie, adjustment less than 18s) during this interval.
     * Bit 1: There was a clock set (ie, adjustment more than 18s) during this interval.
     * Bit 2: This interval log entry was generated automatically because of a time advance as a result of a clock set.
     * Bit 3: There was an invalid volume increment in this.interval. The excess is rolled over to the next interval.
     * Bit 4: The current tariff structure in force during this interval was invalid.
     * Bit 5: A software restart occurred during this interval.
     * Bit 6: The datetime of the interval may be incorrect (a clock sync or set is needed to initialise the clock).
     * Bit 7: Unused.
     */
    private final int statusFlags;  // 2 bytes
    /**
     * Not in use in this product
     */
    private final int rateFlags;    // 2 bytes

    /**
     * Constructor for testing purposes
     */
    public EI4UmiMeterReading(long reading, Date timestamp, int statusFlags, int rateFlags) {
        super(SIZE);
        long timestampSeconds = TimeUnit.MILLISECONDS.toSeconds(timestamp.getTime());
        Date dateTimestamp = new Date(TimeUnit.SECONDS.toMillis(timestampSeconds));

        this.reading = reading;
        this.timestamp = dateTimestamp;
        this.statusFlags = statusFlags;
        this.rateFlags = rateFlags;

        getRawBuffer().putInt((int) this.reading)
                .putInt((int) UmiHelper.convertToUmiFormatFromDate(this.timestamp))
                .putShort((short) statusFlags)
                .putShort((short) rateFlags);

    }

    public EI4UmiMeterReading(byte[] raw) {
        super(raw, SIZE, false);
        reading = Integer.toUnsignedLong(getRawBuffer().getInt());
        timestamp = UmiHelper.convertToDateFromUmiFormat(Integer.toUnsignedLong(getRawBuffer().getInt()));
        statusFlags = Short.toUnsignedInt(getRawBuffer().getShort());
        rateFlags = Short.toUnsignedInt(getRawBuffer().getShort());
    }

    public long getReading() {
        return reading;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public int getStatusFlags() {
        return statusFlags;
    }

    public int getRateFlags() {
        return rateFlags;
    }
}
