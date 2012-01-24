/*
 * Clock.java
 *
 * Created on 19 augustus 2004, 9:28
 */

package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author  Koen
 */
public class Clock extends AbstractCosemObject {

	/** This is the maximum number of seconds we can use a time shift for. */
	public static final int MAX_TIME_SHIFT_SECONDS = 900;

	/** Indicates whether we allow debugging output. */
	private static final int DEBUG = 0;

    /** Method ID of the shift_clock method. */
	private static final int	METHODID_SHIFT_TIME		= 6;
	private static final int	METHODID_SHIFT_TIME_SN	= 0x88;

    /** Long name of the clock object as defined in the spec. */
    private static final byte[] LN = new byte[] { 0, 0, 1, 0, 0, (byte)255 };

    Date dateTime;
    boolean dateTimeCached=false;
    int timeZone=-1;
    int status;
    Date dsDateTimeBegin=null;
    Date dsDateTimeEnd=null;
    int dsDeviation=-1;
    int dsEnabled=-1;
    int dstFlag=-1;

    /** Creates a new instance of Clock */
    public Clock(ProtocolLink protocolLink) {
        super(protocolLink,new ObjectReference(LN));
    }
    public Clock(ProtocolLink protocolLink,ObjectReference objectReference) {
        super(protocolLink,objectReference);
    }

    public static ObisCode getDefaultObisCode() {
		return ObisCode.fromByteArray(LN) ;
	}

    public static boolean isClockObisCode(ObisCode obisCode) {
        return getDefaultObisCode().equals(obisCode);
    }

    /**
     * Set the cached date time object, using the old OctetString object
     *
     * @param dateTime The date and time, encoded in an old {@link com.energyict.dlms.OctetString} object.
     * @throws IOException
     * @deprecated Please use the correct OctetString: {@link Clock#setDateTime(com.energyict.dlms.axrdencoding.OctetString)}
     */
    @Deprecated
    public void setDateTime(com.energyict.dlms.OctetString dateTime) throws IOException {
        setDateTime(OctetString.fromOldOctetString(dateTime));
    }

    /**
     * Set the cached date time object, encoded as a 12 byte {@link com.energyict.dlms.axrdencoding.OctetString}
     *
     * @param dateTime The date time, encoded as a 12 byte OctetString as described in the DLMS blue book
     * @throws IOException
     */
    public void setDateTime(OctetString dateTime) throws IOException {
        this.dateTime = getDateTime(dateTime.getBEREncodedByteArray());
        dateTimeCached = true;
    }

    /**
     * Getter for property dateTime.
     * Apply roundTripCorrection
     * @return Value of property dateTime.
     */
    public Date getDateTime() throws IOException {
        if (dateTimeCached) {
			return dateTime;
		} else {
            byte[] responseData;
            responseData = getResponseData(TIME_TIME);
            if (DEBUG == 1) {
				ProtocolUtils.printResponseData(responseData);
			}
            return getDateTime(responseData,protocolLink.getRoundTripCorrection());
        }
    }

    /**
     * Getter for property dateTime. Method used when attribute of Clock object
     * taken from profile buffer data!
     * Do not apply roundTripCorrection!
     * @param responseData to parse
     * @return Value of property dateTime.
     */
    public Date getDateTime(byte[] responseData) throws IOException {
        return getDateTime(responseData,-1);
    }

    private Date getDateTime(byte[] responseData,int roundtripCorrection) throws IOException {
        Calendar gcalendarMeter=null;

        if ((responseData[13]&(byte)0x80)==(byte)0x80) {
			dstFlag = 1;
		} else {
			dstFlag = 0;
		}


        gcalendarMeter = buildCalendar(responseData);

        if (roundtripCorrection != -1) {
			dateTime = new Date(gcalendarMeter.getTime().getTime()-(long)roundtripCorrection);
		} else {
			dateTime = new Date(gcalendarMeter.getTime().getTime());
		}
        return dateTime;
    }

    private Calendar buildCalendar(byte[] responseData) throws IOException {
        Calendar gcalendarMeter=null;

        int status = (int)responseData[13] & 0xFF;
        if (status != 0xFF) {
            if (protocolLink.isRequestTimeZone()) {
                int reqTimeZone = getTimeZone();
                gcalendarMeter = ProtocolUtils.getCalendar((responseData[13]&(byte)0x80)==(byte)0x80,reqTimeZone);
            } else {
				gcalendarMeter = ProtocolUtils.initCalendar((responseData[13]&(byte)0x80)==(byte)0x80,protocolLink.getTimeZone());
			}
        }
        else {
            gcalendarMeter = ProtocolUtils.getCleanCalendar(protocolLink.getTimeZone());
        }

        int year = (int)ProtocolUtils.getShort(responseData,2)&0x0000FFFF;
        if (year != 0xFFFF) {
			gcalendarMeter.set(Calendar.YEAR,year);
		}

        int month = (int)responseData[4]&0xFF;
        if (month != 0xFF) {
			gcalendarMeter.set(Calendar.MONTH,month-1);
		}

        int date = (int)responseData[5]&0xFF;
        if (date != 0xFF) {
			gcalendarMeter.set(Calendar.DAY_OF_MONTH,date);
		}

        int hour = (int)responseData[7]&0xFF;
        if (hour != 0xFF) {
			gcalendarMeter.set(Calendar.HOUR_OF_DAY,hour);
		}

        int minute = (int)responseData[8]&0xFF;
        if (minute != 0xFF) {
			gcalendarMeter.set(Calendar.MINUTE,minute);
		}

        int seconds = (int)responseData[9]&0xFF;
        if (seconds != 0xFF) {
			gcalendarMeter.set(Calendar.SECOND,seconds);
		}

        gcalendarMeter.set(Calendar.MILLISECOND,0);

        return gcalendarMeter;
    }

    /**
     * Getter for property timeZone.
     * @return Value of property timeZone.
     */
    public int getTimeZone() throws IOException {
        if (timeZone == -1) {
            timeZone = (int)getLongData(TIME_TIME_ZONE)*(-1)/60;
        }
        return timeZone;
    }


    /**
     * Getter for property status.
     * @return Value of property status.
     */
    public int getStatus() throws IOException {
        status = (int)getLongData(TIME_STATUS);
        return status;
    }


    /**
     * Getter for property dsDateTimeBegin.
     * @return Value of property dsDateTimeBegin.
     */
    public Date getDsDateTimeBegin() throws IOException {
        if (dsDateTimeBegin == null) {
            byte[] responseData = getResponseData(TIME_DS_BEGIN);
            dsDateTimeBegin = buildCalendar(responseData).getTime();
        }
        return dsDateTimeBegin;
    }


    /**
     * Getter for property dsDateTimeEnd.
     * @return Value of property dsDateTimeEnd.
     */
    public Date getDsDateTimeEnd() throws IOException {
        if (dsDateTimeEnd == null) {
            byte[] responseData = getResponseData(TIME_DS_END);
            dsDateTimeEnd = buildCalendar(responseData).getTime();
        }
        return dsDateTimeEnd;
    }

    /**
     * Getter for property dsDeviation.
     * @return Value of property dsDeviation.
     */
    public int getDsDeviation() throws IOException {
        if (dsDeviation == -1) {
            dsDeviation = (int)getLongData(TIME_DS_DEVIATION);
        }
        return dsDeviation;
    }

    /**
     * Getter for property dsEnabled.
     * @return Value of property dsEnabled.
     */
    public boolean isDsEnabled() throws IOException {
        if (dsEnabled == -1) {
            dsEnabled = (int)getLongData(TIME_DAYLIGHT_SAVING);
        }
        return dsEnabled==1;
    }

    public int getDstFlag() throws IOException {
        if (dstFlag == -1) {
			throw new IOException("Clock, getDstFlag, dstFlag not evaluated. getDateTime() should invoked first.");
		}
        return dstFlag;
    }

    protected int getClassId() {
        return DLMSClassId.CLOCK.getClassId();
    }

    public void setTimeAttr(DateTime dateTime) throws IOException {
        write(2, dateTime.getBEREncodedByteArray());
    }

    public void setAXDRDateTimeAttr(AXDRDateTime dateTime) throws IOException{
    	write(2,dateTime.getBEREncodedByteArray());
    }

    public AbstractDataType getTimeAttr() throws IOException {
        return AXDRDecoder.decode(getLNResponseData(2));
    }

    /**
     * Shifts the time of the clock by offset seconds. Offset needs to be between -900 and +900 seconds.
     *
     * This translates to method_id 6 on the clock object (class_id 8) in the DLMS blue book. Parameter is a long between -900 seconds and 900
     * seconds.
     *
     * This method can be used to shift time relatively as opposed to setting the time absolutely. This allows for roundtrip correction when using
     * slow communication media.
     *
     * @param 	offset						The offset to shift the time by (in seconds).
     *
     * @throws	IOException					If an IO error occurs during the method invocation.
     * @throws	IllegalArgumentException	In case the offset does not fall between the specified boundaries (-900s <= offset <= 900s).
     */
    public final void shiftTime(final int offset) throws IOException {
    	if ((offset < (MAX_TIME_SHIFT_SECONDS * -1)) || (offset > MAX_TIME_SHIFT_SECONDS)) {
    		throw new IllegalArgumentException("Offset must be between -" + MAX_TIME_SHIFT_SECONDS + " and " + MAX_TIME_SHIFT_SECONDS + " seconds (inclusive) (DLMS blue book 4.5.1), you specified [" + offset + "]");
    	}

    	if (getObjectReference().isLNReference()) {
    		this.invoke(METHODID_SHIFT_TIME, new Integer16(offset).getBEREncodedByteArray());
		} else {
    		this.invoke(METHODID_SHIFT_TIME_SN, new Integer16(offset).getBEREncodedByteArray());
    	}
    }
}
