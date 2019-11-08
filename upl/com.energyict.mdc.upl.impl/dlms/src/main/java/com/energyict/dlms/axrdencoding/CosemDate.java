package com.energyict.dlms.axrdencoding;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.math.BigDecimal;

public class CosemDate extends AbstractDataType {
    protected static final int INT_HIGH_MASK = 0XFF00;
    protected static final int INT_LOW_MASK = 0X00FF;
    protected static final int BITS_PER_BYTE = 8;

    private final int size = 6; // fixed

    private int year;
    private int month;
    private int dayOfMonth;
    private int dayOfWeek;

    public CosemDate(byte[] berEncodedData, int offset) throws ProtocolException {
        int workingOffset = offset;
        if (berEncodedData[workingOffset] != AxdrType.DATE.getTag()) {
            throw new ProtocolException("Date, invalid identifier " + berEncodedData[workingOffset]);
        }
        workingOffset++;
        year = ProtocolUtils.getShort(berEncodedData, workingOffset);
        workingOffset += 2;
        month = ProtocolUtils.getByte2Int(berEncodedData, workingOffset);
        workingOffset++;
        dayOfMonth = ProtocolUtils.getByte2Int(berEncodedData, workingOffset);
        workingOffset++;
        dayOfWeek = ProtocolUtils.getByte2Int(berEncodedData, workingOffset);
    }

    @Override
    protected byte[] doGetBEREncodedByteArray() {
        return
                new byte[]{
                        AxdrType.DATE.getTag(),
                        (byte) (size + 1),    // fixed, no need for giving the length
                        (byte) ((year & INT_HIGH_MASK) >> BITS_PER_BYTE),
                        (byte) (year & INT_LOW_MASK),
                        (byte) (month),
                        (byte) (dayOfMonth),
                        (byte) (dayOfWeek),
                };
    }

    @Override
    public OctetString getOctetString() {
        byte[] bytes = toBytes();
        return OctetString.fromByteArray(bytes);
    }

    public byte[] toBytes() {
        return new byte[]{
                    (byte) ((year & INT_HIGH_MASK) >> BITS_PER_BYTE),
                    (byte) (year & INT_LOW_MASK),
                    (byte) (month),
                    (byte) (dayOfMonth),
                    (byte) (dayOfWeek),
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
        String yearString;
        String monthString;
        String dayOfMonthString;
        DayOfWeek weekday;
        String day = "not set";
        if (yearIsSet()) {
            yearString = Integer.toString(year);
        } else {
            yearString = "Every year on";
        }
        if (monthIsSet()) {
            monthString = format2digets(month);
        } else {
            monthString = "monthly";
        }
        if (dayOfMonthIsSet()) {
            dayOfMonthString = format2digets(dayOfMonth);
        } else {
            dayOfMonthString = "daily";
        }
        if (dayOfWeekIsSet()) {
            weekday = DayOfWeek.fromValue(dayOfWeek);
        } else {
            weekday = DayOfWeek.NOT_SET;
        }
        if (!yearIsSet() || !monthIsSet()) {
            if (dayOfMonthIsSet() && dayOfWeekIsSet()) {
                return String.join(" ", "First", weekday.getStringValue(), "after", dayOfMonthString);
            }
            if (dayOfMonthIsSet() && !dayOfWeekIsSet()) {
                day = dayOfMonthString;
            }
            if (!dayOfMonthIsSet() && dayOfWeekIsSet()) {
                day = weekday.getStringValue();
            }
            if (!dayOfMonthIsSet() && dayOfWeekIsSet() && !yearIsSet() && !monthIsSet()) {
                return String.join(" ", "Every", weekday.getStringValue());
            }
            if (!dayOfMonthIsSet() && dayOfWeekIsSet() && yearIsSet() && !monthIsSet()) {
                return String.join(" ", "Every", weekday.getStringValue(), "in year", yearString);
            }
            if (!dayOfMonthIsSet() && dayOfWeekIsSet() && !yearIsSet() && monthIsSet()) {
                return String.join(" ", "Every", weekday.getStringValue(), "in month", monthString);
            }
        } else {
            day = dayOfMonthString;
        }
        return String.join("-", yearString, monthString, day);
    }


    private String format2digets(int value) {
        return String.format("%02d", value);
    }

    private boolean dayOfWeekIsSet() {
        return dayOfWeek > 0 && dayOfWeek <= 7;
    }

    private boolean dayOfMonthIsSet() {
        return dayOfMonth > 0 && dayOfMonth <= 31;
    }

    private boolean monthIsSet() {
        return month > 0 && month <= 12;
    }

    private boolean yearIsSet() {
        return year > 0;
    }

    private enum DayOfWeek {
        NOT_SET(0, "weekday not set"),
        MONDAY(1, "Monday"),
        TUESDAY(2, "Tuesday"),
        WEDNESDAY(3, "Wednesday"),
        THURSDAY(4, "Thursday"),
        FRIDAY(5, "Friday"),
        SATURDAY(6, "Saturday"),
        SUNDAY(7, "Sunday");

        private int value;
        private String stringValue;

        DayOfWeek(int value, String stringValue) {
            this.value = value;
            this.stringValue = stringValue;
        }

        static DayOfWeek fromValue(int value) {
            for (DayOfWeek day : values())
                if (day.getValue() == value) {
                    return day;
                }
            return NOT_SET;
        }

        public int getValue() {
            return value;
        }

        public String getStringValue() {
            return stringValue;
        }
    }
}
