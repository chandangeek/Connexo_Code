package com.energyict.dlms.axrdencoding.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.protocol.ProtocolUtils;

/**
 * @author gna
 * @since 03/02/2009
 * This is the copy of the DateTime class.
 * The blue book describes the dateTime as an OctetString(size(12)). The 12 indicates that the length is fixed an does not have to be encode.
 *
 * <pre>
 *
 * deviation
 *
 *  long:        in minutes of local time to GMT
 *               0x8000 = not specified
 *
 * clock_status
 *
 *  bit 0 (LSB): invalid value (Time could not be recovered after an incident.)
 *  bit 1:       doubtfull vlaue
 *  bit 2:       different clock base
 *  bit 3:       invalid clock status
 *  bit 4:       reserved
 *  bit 5:       reserved
 *  bit 6:       reserved
 *  bit 7 (MSB): daylist saving active
 *
 *
 * date_time
 *
 *  year highbyte;
 *  year lowbyte;
 *  month;
 *  day of month;
 *  day of week;
 *  hour;
 *  minute;
 *  second;
 *  hundredths of second;
 *  deviation highbyte;
 *  defiation lowbyte;
 *  clock status;
 *
 *
 * day of week is ignored: calendar knows this
 * deviation is ignored: protocol configuration provides timezone
 *
 * </pre>
 *
 */

public class AXDRDateTime extends AbstractDataType {

	private static final int	MILLIS_PER_SECOND					= 1000;
	private static final int	SECONDS_PER_MINUTE					= 60;
	private static final int	MS_PER_HS							= 10;
	private static final int	BITS_PER_BYTE						= 8;

	private static final int	INT_HIGH_MASK						= 0XFF00;
	private static final int	INT_LOW_MASK						= 0X00FF;
	private static final int	INVALID_CLOCK_STATUS_MASK			= 0x08;
	private static final int	DIFFERENT_CLOCK_BASE_STATUS_MASK	= 0x04;
	private static final int	DOUBTFUL_STATUS_MASK				= 0x02;
	private static final int	INVALID_STATUS_MASK					= 0x01;

	private static final byte[]	NO_DEVIATION						= new byte[] { (byte) 0x80, (byte) 0x00 };
	private static final int	SIZE								= 12;

	private Calendar dateTime;
    private int status;

    public AXDRDateTime( ) {
    }

    public AXDRDateTime(TimeZone timeZone) {
    	dateTime = Calendar.getInstance(timeZone);
    }

    public AXDRDateTime(Calendar dateTime) {
    	this.dateTime = dateTime;
    }

    public AXDRDateTime(Date date) {
    	dateTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    	dateTime.setTime(date);
    }

    public AXDRDateTime(OctetString octetString) throws IOException {
    	this(octetString.getBEREncodedByteArray());
    }

    public AXDRDateTime(byte[] berEncodedData) throws IOException {
    	this(berEncodedData,0);
    }

    public AXDRDateTime(byte[] berEncodedData, int offset) throws IOException {
    	int ptr = offset;
    	if (berEncodedData[ptr] != DLMSCOSEMGlobals.TYPEDESC_OCTET_STRING){
            throw new IOException("AXDRDateTime, invalid identifier "+berEncodedData[ptr]);
    	}
    	ptr = ptr + 2;

		int deviation = 0;
		if ((berEncodedData[offset + 11] != NO_DEVIATION[0]) || (berEncodedData[offset + 12] != NO_DEVIATION[1])) {
			int tOffset = (short) ProtocolUtils.getInt(berEncodedData, offset + 11, 2);
			tOffset *= -1;
			deviation = tOffset / SECONDS_PER_MINUTE;
		}

        TimeZone tz = TimeZone.getTimeZone("GMT"+(deviation<0?"":"+")+deviation);
    	dateTime = Calendar.getInstance(tz);

        int year = ProtocolUtils.getShort(berEncodedData, ptr );
        dateTime.set(Calendar.YEAR, year);
        ptr = ptr + 2;

        int month = ProtocolUtils.getByte2Int(berEncodedData, ptr);
        dateTime.set(Calendar.MONTH, month-1);
        ptr = ptr + 1;

        int dayOfMonth = ProtocolUtils.getByte2Int(berEncodedData, ptr);
        dateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        ptr = ptr + 2; // one extra: skip day of week

        int hour = ProtocolUtils.getByte2Int(berEncodedData, ptr);
        dateTime.set(Calendar.HOUR_OF_DAY, hour);
        ptr = ptr + 1;

        int minute = ProtocolUtils.getByte2Int(berEncodedData, ptr);
        dateTime.set(Calendar.MINUTE, minute);
        ptr = ptr + 1;

        int second = ProtocolUtils.getByte2Int(berEncodedData, ptr);
        dateTime.set(Calendar.SECOND, second);

        dateTime.set(Calendar.MILLISECOND, 0);

        ptr = ptr + 1;

        ptr = ptr + 1;

        ptr = ptr + 1;    // deviation highbyte

        ptr = ptr + 1;    // deviation lowbyte

        status = ProtocolUtils.getByte2Int(berEncodedData, ptr);


    }

    public int getStatus() {
        return status;
    }

    protected byte[] doGetBEREncodedByteArray() {

        Calendar v = getValue();

        int year        = v.get(Calendar.YEAR);
        int month       = v.get(Calendar.MONTH);
        int dayOfMonth  = v.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek   = v.get(Calendar.DAY_OF_WEEK);
        int hour        = v.get(Calendar.HOUR_OF_DAY);
        int minute      = v.get(Calendar.MINUTE);
        int second      = v.get(Calendar.SECOND);
        int hs          = v.get(Calendar.MILLISECOND) / MS_PER_HS;

//        int deviation = (v.getTimeZone().getRawOffset()/60000) + (v.getTimeZone().inDaylightTime(v.getTime())?1:0);

        return
            new byte [] {
                DLMSCOSEMGlobals.TYPEDESC_OCTET_STRING,
                (byte) SIZE,	// fixed octetString, no need for giving the length
                (byte) ((year & INT_HIGH_MASK ) >> BITS_PER_BYTE),
                (byte) (year & INT_LOW_MASK),
                (byte) (month + 1),
                (byte) (dayOfMonth),
                (byte) (dayOfWeek - 1),
                (byte) (hour),
                (byte) (minute),
                (byte) (second),
                (byte) (hs),
//                (byte) ((deviation>>8)&0xFF),
//                (byte) (deviation&0xFF),
                (byte) 0x00,
                (byte) 0x00,
                (byte)status
            };

    }

    protected int size() {
        return SIZE;
    }

    public void setValue(Calendar dateTime) {
        setValue(dateTime, (byte)0);
    }

    public void setValue(Calendar dateTime, byte status) {
        this.dateTime = dateTime;
        this.status = status;
    }

    public Calendar getValue( ){
        return dateTime;
    }

    public boolean isInvalid() {
        return (status & INVALID_STATUS_MASK) > 0;
    }

    public boolean isDoubtful() {
        return (status & DOUBTFUL_STATUS_MASK) > 0;
    }

    public boolean isDifferentClockBase() {
        return (status & DIFFERENT_CLOCK_BASE_STATUS_MASK) > 0;
    }

    public boolean isInvalidClockStatus() {
        return (status & INVALID_CLOCK_STATUS_MASK) > 0;
    }

    public BigDecimal toBigDecimal() {
        return BigDecimal.valueOf(getValue().getTime().getTime());
    }

    public int intValue() {
        return (int)(getValue().getTime().getTime()/MILLIS_PER_SECOND);
    }

    public long longValue() {
        return getValue().getTime().getTime();
    }

    @Override
	public String toString() {
		String rawData = ProtocolUtils.getResponseData(getBEREncodedByteArray());
		return getValue().getTime().toString() + " [" + rawData + "]";
	}

}
