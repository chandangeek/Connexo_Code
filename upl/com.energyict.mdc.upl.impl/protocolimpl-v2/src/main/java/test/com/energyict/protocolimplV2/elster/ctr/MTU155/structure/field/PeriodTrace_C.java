package test.com.energyict.protocolimplV2.elster.ctr.MTU155.structure.field;

import com.energyict.cbo.TimeDuration;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.common.AbstractField;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.exception.CTRParsingException;

/**
 * Class for the PeriodTrace_C field in a CTR Structure Object
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:39:27
 */
public class PeriodTrace_C extends AbstractField<PeriodTrace_C> {

    private static final int HOURLY = 1;
    private static final int DAILY = 2;
    private static final int MONTHLY = 3;

    private static final int LENGTH = 1;
    private static final TimeDuration HOUR = new TimeDuration(1, TimeDuration.HOURS);
    private static final TimeDuration DAY = new TimeDuration(1, TimeDuration.DAYS);
    private static final TimeDuration MONTH = new TimeDuration(1, TimeDuration.MONTHS);

    private int period;

    public PeriodTrace_C() {
        this(0);
    }

    public PeriodTrace_C(TimeDuration interval) {
        int seconds = interval.getSeconds();
        if (seconds == HOUR.getSeconds()) {
            this.period = HOURLY;
        } else if (seconds == DAY.getSeconds()) {
            this.period = DAILY;
        } else if (seconds == MONTH.getSeconds()) {
            this.period = MONTHLY;
        } else {
            throw new IllegalArgumentException("Unable to get PeriodTrace_C object for a TimeDuration of [" + interval.toString() + "]");
        }
    }

    public static PeriodTrace_C getHourly() {
        return new PeriodTrace_C(HOURLY);
    }

    public static PeriodTrace_C getDaily() {
        return new PeriodTrace_C(DAILY);
    }

    public static PeriodTrace_C getMonthly() {
        return new PeriodTrace_C(MONTHLY);
    }

    public PeriodTrace_C(int period) {
        this.period = period;
    }

    public byte[] getBytes() {
        return getBytesFromInt(period, getLength());
    }

    public PeriodTrace_C parse(byte[] rawData, int offset) throws CTRParsingException {
        this.period = getIntFromBytes(rawData, offset, getLength());
        return this;
    }

    public int getLength() {
        return LENGTH;
    }

    public int getPeriod() {
        return period;
    }

    /**
     * @return possible date formats
     */
    public String getDateFormat() {
        switch (period) {
            case HOURLY:
                return "yy, mm, dd";
            case DAILY:
                return "yy, mm, dd";
            case MONTHLY:
                return "yy, mm, 00";
            default:
                return "";
        }
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    /**
     * @return description for the period of the trace_c data
     */
    public String getDescription() {
        switch (period) {
            case HOURLY:
                return "All 1h traces on the specified day";
            case DAILY:
                return "The 1-day traces for the last 15 days (that specified included)";
            case MONTHLY:
                return "The 1-month traces for the last 12 months (that specified included)";
            default:
                return "";
        }
    }

    /**
     * @return the interval in seconds
     */
    public int getIntervalInSeconds() {
        switch (period) {
            case HOURLY:
                return HOUR.getSeconds();
            case DAILY:
                return DAY.getSeconds();
            case MONTHLY:
                return MONTH.getSeconds();
            default:
                return 0;
        }
    }

    public int getTraceCIntervalCount() {
        switch (period) {
            case HOURLY:
                return 24; // 24 hours
            case DAILY:
                return 15;   // 15 Days
            case MONTHLY:
                return 12; // 12 Months
            default:
                return 0;
        }
    }

    public boolean isHourly() {
        return period == HOURLY;
    }

    public boolean isDaily() {
        return period == DAILY;
    }

    public boolean isMonthly() {
        return period == MONTHLY;
    }

    public boolean isInvalid() {
        return !(isDaily() || isHourly() || isMonthly());
    }

}
