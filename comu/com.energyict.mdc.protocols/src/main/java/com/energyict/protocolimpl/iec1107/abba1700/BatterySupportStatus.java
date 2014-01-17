package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 * Represents the BatterySupport status.
 */
public class BatterySupportStatus {

    public static final int DateTimeLength = 4;
    protected static final long DateTimeMask = 0xFFFFFFFFL;
    protected static final long NrOfSecondsInDay = 86400;

    protected final ProtocolLink protocolLink;
    private Date originalBatteryCapacity;
    private int remainingBatterySupportTime;
    private int remainingPowerDownBatteryLife;

    public BatterySupportStatus(final ProtocolLink protocolLink, final byte[] data) throws IOException {
        this.protocolLink = protocolLink;
        long shift = (long) ProtocolUtils.getIntLE(data, 0, DateTimeLength) & DateTimeMask;
        setOriginalBatteryCapacity(ProtocolUtils.getCalendar(getTimeZone(), shift).getTime());
        shift = (long) ProtocolUtils.getIntLE(data, DateTimeLength, DateTimeLength) & DateTimeMask;
        setRemainingBatterySupportTime((int) (shift / NrOfSecondsInDay));
        shift = (long) ProtocolUtils.getIntLE(data, DateTimeLength + DateTimeLength, DateTimeLength) & DateTimeMask;
        setRemainingPowerDownBatteryLife((int) (shift / NrOfSecondsInDay));
    }

    private TimeZone getTimeZone() {
        return this.protocolLink.getTimeZone();
    }

    public void setOriginalBatteryCapacity(final Date originalBatteryCapacity) {
        this.originalBatteryCapacity = originalBatteryCapacity;
    }

    public void setRemainingBatterySupportTime(final int remainingBatterySupportTime) {
        this.remainingBatterySupportTime = remainingBatterySupportTime;
    }

    public void setRemainingPowerDownBatteryLife(final int remainingPowerDownBatteryLife) {
        this.remainingPowerDownBatteryLife = remainingPowerDownBatteryLife;
    }

    /**
     * Getter for the installationDate of the battery
     *
     * @return the installationDate of the battery
     */
    public Date getBatteryInstallDate() {
        return originalBatteryCapacity;
    }

    /**
     * Getter for the remaining nr. of days the battery will last (self discharge)
     *
     * @return the remaining nr. of days the battery will last (self discharge)
     */
    public int getRemainingBatterySupportTime() {
        return remainingBatterySupportTime;
    }

    /**
     * Getter for the remaining nr. of days the battery will last on powerdown
     *
     * @return the remaining nr. of days the battery will last on powerdown
     */
    public int getRemainingPowerDownBatteryLife() {
        return remainingPowerDownBatteryLife;
    }
}