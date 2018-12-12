package com.energyict.protocolimpl.dlms.g3.profile;

import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.protocolimpl.utils.ProtocolUtils;

public class G3LoadProfileEntry {

    private static final int[] INTERVAL_VALUES = new int[]{
            10 * 60,
            15 * 60,
            30 * 60,
            60 * 60
    };

    private static final int POWER_TIME_BIT = 31;
    private static final int PARTIAL_BIT = 30;
    private static final int DATE_TIME_BIT = 30;
    private static final int DEGRADED = 29;

    private static final int INTERVAL_LSB = 22;
    private static final int DATE_TIME_REASON_LSB = 24;

    private static final int YEAR_LSB = 9;
    private static final int MONTH_LSB = 5;
    private static final int HOURS_LSB = 12;
    private static final int MINUTES_LSB = 6;

    private final long value;

    public G3LoadProfileEntry(final long value) {
        this.value = value;
    }

    public G3LoadProfileEntry(final Unsigned32 value) {
        this.value = value.getValue();
    }

    public final boolean isValue() {
        return !isDateTime();
    }

    public final boolean isDateTime() {
        return isBitSet(POWER_TIME_BIT);
    }

    public final boolean isDate() {
        return isDateTime() && !isTime();
    }

    public final boolean isTime() {
        return isDateTime() && isBitSet(DATE_TIME_BIT);
    }

    public final boolean isNormalValue() {
        return isValue() && !isPartialValue();
    }

    public final boolean isPartialValue() {
        return isValue() && isBitSet(PARTIAL_BIT);
    }

    public final boolean isDegraded() {
        return isBitSet(DEGRADED);
    }

    /**
     * Check if the n-th bit is set of the value. The bit number is 0 based, so it can range from 0 to 31.
     *
     * @param bitNumber The number of the bit to test
     * @return True if the bit was set, false if not.
     */
    private final boolean isBitSet(int bitNumber) {
        return ((value >> bitNumber) & 0x01) == 1;
    }

    public final int getIntervalInSeconds() {
        return INTERVAL_VALUES[((int) ((value >> INTERVAL_LSB) & 0x03))];
    }

    public final long getValue() {
        return isValue() ? value & 0x01FFFFF : 0;
    }

    public final int getYear() {
        return isDate() ? (int) ((value >> YEAR_LSB) & 0x07F) + 2000 : 0;
    }

    public final int getMonth() {
        return isDate() ? (int) ((value >> MONTH_LSB) & 0x0F) : 0;
    }

    public final int getDay() {
        return isDate() ? (int) (value & 0x01F) : 0;
    }

    public final int getHours() {
        return isTime() ? (int) ((value >> HOURS_LSB) & 0x01F) : 0;
    }

    public final int getMinutes() {
        return isTime() ? (int) ((value >> MINUTES_LSB) & 0x03F) : 0;
    }

    public final int getSeconds() {
        return isTime() ? (int) (value & 0x03F) : 0;
    }

    public final boolean isStartOfLoadProfile() {
        return getDateTimeReason() == DateTimeReason.START_OF_PROFILE;
    }

    public final boolean isPowerOff() {
        return getDateTimeReason() == DateTimeReason.POWER_DOWN;
    }

    public final boolean isPowerOn() {
        return getDateTimeReason() == DateTimeReason.POWER_UP;
    }

    public final boolean isChangeclockOldTime() {
        return getDateTimeReason() == DateTimeReason.OLD_CLOCK;
    }

    public final boolean isChangeclockNewTime() {
        return getDateTimeReason() == DateTimeReason.NEW_CLOCK;
    }

    private final DateTimeReason getDateTimeReason() {
        return isDateTime() ? DateTimeReason.fromValue((int) ((value >> DATE_TIME_REASON_LSB) & 0x007)) : DateTimeReason.UNKNOWN;
    }

    private enum DateTimeReason {
        START_OF_PROFILE(0),
        CHANGE_OF_INTERVAL(1),
        POWER_DOWN(2),
        POWER_UP(3),
        OLD_CLOCK(4),
        NEW_CLOCK(5),
        UNKNOWN(-1);

        private final int reason;

        DateTimeReason(int reason) {
            this.reason = reason;
        }

        public int getReason() {
            return reason;
        }

        public static DateTimeReason fromValue(int value) {
            final DateTimeReason[] values = values();
            for (DateTimeReason reason : values) {
                if (reason.getReason() == value) {
                    return reason;
                }
            }
            return UNKNOWN;
        }

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("G3LoadProfileEntry");
        sb.append(" { rawValue=").append(ProtocolUtils.buildStringHex(value, 8)).append(", ");

        if (isDate()) {
            sb.append("reason=").append(getDateTimeReason()).append(", ");
            sb.append("date=");
            sb.append(getYear()).append('/');
            sb.append(getMonth()).append('/');
            sb.append(getDay());
        }

        if (isTime()) {
            sb.append("reason=").append(getDateTimeReason()).append(", ");
            sb.append("time=");
            sb.append(getHours()).append(':');
            sb.append(getMinutes()).append(':');
            sb.append(getSeconds());
        }

        if (isNormalValue()) {
            sb.append("value (normal)=").append(getValue());
            sb.append(", interval=").append(getIntervalInSeconds());
        }

        if (isPartialValue()) {
            sb.append("value (partial)=").append(getValue());
            sb.append(", interval=").append(getIntervalInSeconds());
        }

        sb.append('}');
        return sb.toString();
    }

}
