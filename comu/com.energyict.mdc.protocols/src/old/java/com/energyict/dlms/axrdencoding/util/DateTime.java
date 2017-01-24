package com.energyict.dlms.axrdencoding.util;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
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
 * @author fbo
 *
 */

public class DateTime extends AbstractDataType {

	private static final int	BYTE_SIZE					= 8;
	private static final int	H_PER_SEC					= 10;
	private static final int	MS_PER_SEC					= 1000;
	private static final int	SIZE						= 0x0c;

	private static final int	INT_LOW_MASK				= 0X00FF;
	private static final int	INT_HIGH_MASK				= 0XFF00;
	private static final int	INVALID_MASK				= 0x01;
	private static final int	DOUBTFUL_MASK				= 0x02;
	private static final int	DIFFERENT_CLOCK_BASE_MASK	= 0x04;
	private static final int	INVALID_CLOCK_MASK			= 0x08;


	private Calendar dateTime;
    private int status;

    public DateTime( ) {
    }

    public DateTime(TimeZone timeZone) {
    	dateTime = Calendar.getInstance(timeZone);
    }

    public DateTime(Calendar dateTime) {
    	this.dateTime = dateTime;
    }

    public DateTime(Date date) {
    	dateTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    	dateTime.setTime(date);
    }

    public DateTime(OctetString octetString) throws IOException {
    	this(octetString.getBEREncodedByteArray());
    }

    public DateTime(OctetString octetString, TimeZone tz) {
    	this(octetString.getBEREncodedByteArray(),0,tz);
    }

    public DateTime(byte[] berEncodedData) {
    	this(berEncodedData,0,TimeZone.getTimeZone("GMT"));
    }

    public DateTime(byte[] berEncodedData, int offset, TimeZone zone) {
        int ptr = offset;

        // !! add type check here !!
        ptr = ptr + 2;

        dateTime = ProtocolUtils.getCleanCalendar(zone);

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
        int hs          = v.get(Calendar.MILLISECOND) / H_PER_SEC;

        return
            new byte [] {
                    AxdrType.OCTET_STRING.getTag(),
                (byte) SIZE,
                (byte) ((year & INT_HIGH_MASK ) >> BYTE_SIZE),
                (byte) (year & INT_LOW_MASK),
                (byte) (month + 1),
                (byte) (dayOfMonth),
                (byte) (dayOfWeek - 1),
                (byte) (hour),
                (byte) (minute),
                (byte) (second),
                (byte) (hs),
                (byte) 0x80,
                0,
                (byte)status
            };

    }

	protected int size() {
        return 1 + 1 + SIZE; // Tag + size byte + actual data (12 bytes)
    }

	public void setValue(Calendar dateTime) {
		setValue(dateTime, (byte) 0);
	}

	public void setValue(Calendar dateTime, byte status) {
		this.dateTime = dateTime;
		this.status = status;
	}

	public Calendar getValue() {
		return dateTime;
	}

	public boolean isInvalid() {
		return (status & INVALID_MASK) > 0;
	}

	public boolean isDoubtful() {
		return (status & DOUBTFUL_MASK) > 0;
	}

	public boolean isDifferentClockBase() {
		return (status & DIFFERENT_CLOCK_BASE_MASK) > 0;
	}

	public boolean isInvalidClockStatus() {
		return (status & INVALID_CLOCK_MASK) > 0;
	}

	public BigDecimal toBigDecimal() {
		return BigDecimal.valueOf(getValue().getTime().getTime());
	}

	public int intValue() {
		return (int) (getValue().getTime().getTime() / MS_PER_SEC);
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
