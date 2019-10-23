package com.energyict.dlms.axrdencoding;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * This class models the date-time object with datatype 0x19 (25 decimal)
 *
 * date_time SIZE(12) which is fixed, no length is given in the object
 * {
 *      year highbyte,
 *      year lowbyte,
 *      month,
 *      day of month,
 *      day of week,
 *      hour,
 *      minute,
 *      second,
 *      hundredths of second,
 *      deviation highbyte,
 *      deviation lowbyte,
 *      clock status
 * }
 *
 * Individual fields of date_time are encoded as defined above. Some
 * may be set to “not specified“ as described above in date and time.
 *
 * <pre>
 *  year:           interpreted as long-unsigned
 *                  range 0…big
 *                  0xFFFF = not specified
 *                  year highbyte and year lowbyte reference the 2 bytes of the longunsigned
 *  month:          interpreted as unsigned
 *                  range 1…12, 0xFD, 0xFE, 0xFF
 *                  1 is January
 *                  0xFD= daylight_savings_end
 *                  0xFE= daylight_savings_begin
 *                  0xFF = not specified
 *  dayOfMonth:     interpreted as unsigned
 *                  range 1…31, 0xFD, 0xFE, 0xFF
 *                  0xFD = 2nd last day of month
 *                  0xFE = last day of month
 *                  0xE0 to 0xFC = reserved
 *                  0xFF = not specified
 *  dayOfWeek:      interpreted as unsigned
 *                  range 1…7, 0xFF
 *                  1 is Monday
 *                  0xFF = not specified
 *
 *  For repetitive dates, the unused parts must be set to “not specified”.
 *  The elements dayOfMonth and dayOfWeek have to be interpreted together.
 *      - if last day of month is specified (0xFE) and day of week is wildcard, this specifies the last calendar day of the month;
 *      - if last day of month is specified (0xFE) and an explicit day of week is specified (for example 7, Sunday) then it is the last occurrence of the weekday specified in the month, i.e. the last Sunday;
 *      - if the dayOfMonth and dayOfWeek elements are both explicitly defined and they are not consistent, (for example 24th of the month is not Wednesday in the given year and month) it shall be considered as an error.
 *
 *  hour:           interpreted as unsigned
 *                  range 0…23, 0xFF
 *                  0xFF = not specified,
 *  minute:         interpreted as unsigned
 *                  range 0…59, 0xFF
 *                  0xFF = not specified,
 *  second:         interpreted as unsigned
 *                  range 0…59, 0xFF
 *                  0xFF = not specified,
 *  hundredths:     interpreted as unsigned
 *                  range 0…99, 0xFF
 *                  0xFF = not specified
 *
 *  For repetitive times the unused parts must be set to “not specified”.
 *
 *  deviation:      long –720…720:
 *                  in minutes of local time to GMT
 *                  0x8000 = not specified
 *
 *  clock_status:   unsigned interpreted as 8 bit string
 *                  bit 0 (LSB):    invalid value (Time could not be recovered after an incident.)
 *                  bit 1:          doubtful value
 *                  bit 2:          different clock base
 *                  bit 3:          invalid clock status
 *                  bit 4:          reserved
 *                  bit 5:          reserved
 *                  bit 6:          reserved
 *                  bit 7 (MSB):    daylist saving active
 *
 * day of week is ignored: calendar knows this
 * deviation is ignored: protocol configuration provides timezone
 *
 * </pre>
 *
 * @author pvm
 *
 */

public class DateTime extends AbstractDataType {

    private static final int BYTE_SIZE					= 8;
    private static final int H_PER_SEC					= 10;
    private static final int MS_PER_SEC					= 1000;
    private static final int SIZE						= 0x0c;

    private static final int NOT_SPECIFIED_1_BYTE_VALUE = 0xFF;
    private static final int NOT_SPECIFIED_2_BYTE_VALUE = 0xFFFF;
    private static final int NOT_SPECIFIED_DEVIATION    = 0x8000;
    private static final int NO_CLOCK_STATUS_BITS_SET   = 0x00;

    private static final int INT_LOW_MASK				= 0X00FF;
    private static final int INT_HIGH_MASK				= 0XFF00;
    private static final int INVALID_MASK				= 0x01;
    private static final int DOUBTFUL_MASK				= 0x02;
    private static final int DIFFERENT_CLOCK_BASE_MASK	= 0x04;
    private static final int INVALID_CLOCK_MASK			= 0x08;

    private int year            = NOT_SPECIFIED_2_BYTE_VALUE;
    private int month           = NOT_SPECIFIED_1_BYTE_VALUE;
    private int dayOfMonth      = NOT_SPECIFIED_1_BYTE_VALUE;
    private int dayOfWeek       = NOT_SPECIFIED_1_BYTE_VALUE;
    private int hour            = NOT_SPECIFIED_1_BYTE_VALUE;
    private int minute          = NOT_SPECIFIED_1_BYTE_VALUE;
    private int second          = NOT_SPECIFIED_1_BYTE_VALUE;
    private int hundredths      = NOT_SPECIFIED_1_BYTE_VALUE;
    private int deviation       = NOT_SPECIFIED_DEVIATION;
    private int clockStatus     = NO_CLOCK_STATUS_BITS_SET;

    public DateTime(byte[] berEncodedData) {
        this(berEncodedData,0, TimeZone.getTimeZone("GMT"));
    }

    public DateTime(byte[] berEncodedData, int offset){
        this(berEncodedData,offset, TimeZone.getTimeZone("GMT"));
    }

    public DateTime(byte[] berEncodedData, int offset, TimeZone timeZone) {
        init(berEncodedData, offset);
    }

    private void init(byte[] berEncodedData, int offset) {
        int ptr = offset;
        if (AxdrType.DATE_TIME.getTag()!=ProtocolUtils.getByte2Int(berEncodedData, ptr++)) {
            // might want to throw an Exception when the AddrType is not equal to DATE_TIME
        }
        year = ProtocolUtils.getShort(berEncodedData, ptr++);
        ptr++;
        month = ProtocolUtils.getByte2Int(berEncodedData, ptr++);
        dayOfMonth = ProtocolUtils.getByte2Int(berEncodedData, ptr++);
        dayOfWeek = ProtocolUtils.getByte2Int(berEncodedData, ptr++);
        hour = ProtocolUtils.getByte2Int(berEncodedData, ptr++);
        minute = ProtocolUtils.getByte2Int(berEncodedData, ptr++);
        second = ProtocolUtils.getByte2Int(berEncodedData, ptr++);
        hundredths = ProtocolUtils.getByte2Int(berEncodedData, ptr++);
        deviation = ProtocolUtils.getShort(berEncodedData, ptr++);
        ptr++;
        clockStatus = ProtocolUtils.getByte2Int(berEncodedData, ptr);
    }

    protected byte[] doGetBEREncodedByteArray() {
        return
                new byte [] {
                        AxdrType.DATE_TIME.getTag(),
                        (byte) ((year & INT_HIGH_MASK ) >> BYTE_SIZE),
                        (byte) (year & INT_LOW_MASK),
                        (byte) (month),
                        (byte) (dayOfMonth),
                        (byte) (dayOfWeek),
                        (byte) (hour),
                        (byte) (minute),
                        (byte) (second),
                        (byte) (hundredths),
                        (byte) ((deviation & INT_HIGH_MASK ) >> BYTE_SIZE),
                        (byte) (deviation & INT_LOW_MASK),
                        (byte) clockStatus
                };

    }

    protected int size() {
        return 1 + SIZE; // Tag + actual data (12 bytes)
    }

    public void setValue(Calendar calendar) {
        year        = calendar.get(Calendar.YEAR);
        month       = calendar.get(Calendar.MONTH);
        dayOfMonth  = calendar.get(Calendar.DAY_OF_MONTH);
        dayOfWeek   = convertDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));
        hour        = calendar.get(Calendar.HOUR_OF_DAY);
        minute      = calendar.get(Calendar.MINUTE);
        second      = calendar.get(Calendar.SECOND);
        hundredths  = calendar.get(Calendar.MILLISECOND) / H_PER_SEC;
    }

    private int convertDayOfWeek(int calendarDayOfWeek) {
        return (calendarDayOfWeek==1) ? 7 : calendarDayOfWeek-1;
    }

    public boolean isValidDate() {
        return  year        != NOT_SPECIFIED_2_BYTE_VALUE &&
                month       != NOT_SPECIFIED_1_BYTE_VALUE &&
                dayOfMonth  != NOT_SPECIFIED_1_BYTE_VALUE &&
                hour        != NOT_SPECIFIED_1_BYTE_VALUE &&
                minute      != NOT_SPECIFIED_1_BYTE_VALUE &&
                second      != NOT_SPECIFIED_1_BYTE_VALUE;
    }

    public Calendar getCalendar(TimeZone timeZone) {
        Calendar dateTime = ProtocolUtils.getCleanCalendar(timeZone);
        dateTime.set(Calendar.YEAR, year);
        dateTime.set(Calendar.MONTH, month-1);
        dateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        dateTime.set(Calendar.HOUR_OF_DAY, hour);
        dateTime.set(Calendar.MINUTE, minute);
        dateTime.set(Calendar.SECOND, second);
        if (hundredths!=NOT_SPECIFIED_1_BYTE_VALUE) {
            dateTime.set(Calendar.MILLISECOND, hundredths * H_PER_SEC);
        }
        return dateTime;
    }

    public Calendar getCalendar() {
        return getCalendar(TimeZone.getTimeZone("GMT"));
    }

    public boolean isInvalid() {
        return (clockStatus & INVALID_MASK) > 0;
    }

    public boolean isDoubtful() {
        return (clockStatus & DOUBTFUL_MASK) > 0;
    }

    public boolean isDifferentClockBase() {
        return (clockStatus & DIFFERENT_CLOCK_BASE_MASK) > 0;
    }

    public boolean isInvalidClockStatus() {
        return (clockStatus & INVALID_CLOCK_MASK) > 0;
    }


    public int intValue() {
        return (int) (getCalendar().getTime().getTime() / MS_PER_SEC);
    }

    public BigDecimal toBigDecimal() {
        return BigDecimal.valueOf(longValue());
    }

    public long longValue() {
        return getCalendar().getTime().getTime();
    }

    @Override
    public String toString() {
        StringBuffer strBuffTab = new StringBuffer();
        for (int i=0;i<getLevel();i++) {
            strBuffTab.append("  ");
        }
        return strBuffTab.toString()+"DateTime="+ProtocolUtils.getResponseData(getBEREncodedByteArray())+"\n";
    }

}
