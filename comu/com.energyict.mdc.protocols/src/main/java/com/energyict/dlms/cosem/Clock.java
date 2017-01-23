/*
 * Clock.java
 *
 * Created on 19 augustus 2004, 9:28
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.Integer16;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.attributes.ClockAttributes;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import static com.energyict.dlms.DLMSCOSEMGlobals.TIME_DAYLIGHT_SAVING;
import static com.energyict.dlms.DLMSCOSEMGlobals.TIME_DS_DEVIATION;
import static com.energyict.dlms.DLMSCOSEMGlobals.TIME_STATUS;
import static com.energyict.dlms.DLMSCOSEMGlobals.TIME_TIME_ZONE;

/**
 *
 * @author  Koen
 */
public class Clock extends AbstractCosemObject {

	/** This is the maximum number of seconds we can use a time shift for. */
	public static final int MAX_TIME_SHIFT_SECONDS = 900;

	/** Indicates whether we allow debugging output. */
	private static final int DEBUG = 0;

    /** Method ID of the adjust_to_preset_time method. */
    private static final int METHODID_ADJUST_TO_PRESET_TIME = 4;
    private static final int METHODID_ADJUST_TO_PRESET_TIME_SN = 0x78;

    /** Method ID of the preset_adjusting_time method. */
    private static final int METHODID_PRESET_ADJUSTING_TIME = 5;
    private static final int METHODID_PRESET_ADJUSTING_TIME_SN = 0x80;

    /** Method ID of the shift_clock method. */
    private static final int METHODID_SHIFT_TIME = 6;
    private static final int METHODID_SHIFT_TIME_SN = 0x88;

    /** Long name of the clock object as defined in the spec. */
    private static final byte[] LN = new byte[] { 0, 0, 1, 0, 0, (byte)255 };

    Date dateTime;
    boolean dateTimeCached=false;
    int timeZone=-1;
    int status;
    byte[] dsDateTimeBegin;
    byte[] dsDateTimeEnd;
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
     * @throws java.io.IOException
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
     * @throws java.io.IOException
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
            responseData = getResponseData(ClockAttributes.TIME);
            if (DEBUG == 1) {
				ProtocolUtils.printResponseData(responseData);
			}
            return getDateTime(responseData,protocolLink.getRoundTripCorrection());
        }
    }

    public AXDRDateTime getAXDRDateTime() throws IOException {
        byte[] responseData = getResponseData(ClockAttributes.TIME);
        AXDRDateTime axdrDateTime = new AXDRDateTime(responseData, 0, protocolLink.getTimeZone());
        return axdrDateTime;
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
                gcalendarMeter = ProtocolUtils.getCalendar((responseData[13] & (byte) 0x80) == (byte) 0x80, reqTimeZone);
            } else {
				gcalendarMeter = ProtocolUtils.initCalendar((responseData[13] & (byte) 0x80) == (byte) 0x80, protocolLink.getTimeZone());
			}
        }
        else {
            gcalendarMeter = ProtocolUtils.getCleanCalendar(protocolLink.getTimeZone());
        }

        int year = (int) ProtocolUtils.getShort(responseData, 2)&0x0000FFFF;
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
     *
     * @return Value of property dsDateTimeBegin.
     */
    public byte[] getDsDateTimeBegin() throws IOException {
        if (dsDateTimeBegin == null) {
            AbstractDataType dataType = AXDRDecoder.decode(getResponseData(ClockAttributes.TIME_DS_BEGIN));
            dsDateTimeBegin = dataType.getOctetString().getOctetStr();
        }
        return dsDateTimeBegin;
    }

    /**
     * Getter for property dsDateTimeEnd.
     *
     * @return Value of property dsDateTimeEnd.
     */
    public byte[] getDsDateTimeEnd() throws IOException {
        if (dsDateTimeEnd == null) {
            AbstractDataType dataType = AXDRDecoder.decode(getResponseData(ClockAttributes.TIME_DS_END));
            dsDateTimeEnd = dataType.getOctetString().getOctetStr();
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
     * Write the start of DST
     *
     * @param dateTime  OctetString(size(12)) containing the AXDRDateTime
     * @throws java.io.IOException
     */
    public void setDsDateTimeBegin(byte[] dateTime) throws IOException {
        write(ClockAttributes.TIME_DS_BEGIN, dateTime);
        dsDateTimeBegin = dateTime;
    }

    /**
     * Write the end of DST
     *
     * @param dateTime  OctetString(size(12)) containing the AXDRDateTime
     * @throws java.io.IOException
     */
    public void setDsDateTimeEnd(byte[] dateTime) throws IOException {
        write(ClockAttributes.TIME_DS_END, dateTime);
        dsDateTimeEnd = dateTime;
    }

    public void setTimeZone(int offset) throws IOException {
        write(ClockAttributes.TIMEZONE, new Integer16(offset).getBEREncodedByteArray());
    }

    /**
     * Getter for property dsEnabled.
     * @return Value of property dsEnabled.
     */
    public boolean isDsEnabled() throws IOException {
        if (dsEnabled == -1) {
            dsEnabled = (int) getLongData(TIME_DAYLIGHT_SAVING);
        }
        return dsEnabled != 0;
    }

    /**
     * WARNING: Execution of this method can cause an immediate clock shift.
     * Procedure to enable/disable daylight switching
     * 1. execute this method to enable or disable the switching
     * 2. Adapt the device time zone, so it reflects the new situation (ex.: change timezone 'GMT+1' to 'Europe/Brussels').
     * 3. Issue a force sync clock - In some cases the device time is shifted 1 hour (occurs when summertime is active).
     *
     * @param enable
     * @throws java.io.IOException
     */
    public void enableDisableDs(boolean enable) throws IOException {
        BooleanObject booleanObject = new BooleanObject(enable);
        write(ClockAttributes.DS_ENABLED, booleanObject.getBEREncodedByteArray());
        dsEnabled = booleanObject.intValue();
    }

    public int getDstFlag() throws IOException {
        if (dstFlag == -1) {
			throw new ProtocolException("Clock, getDstFlag, dstFlag not evaluated. getDateTime() should invoked first.");
		}
        return dstFlag;
    }

    protected int getClassId() {
        return DLMSClassId.CLOCK.getClassId();
    }

    public void setTimeAttr(DateTime dateTime) throws IOException {
        write(ClockAttributes.TIME, dateTime.getBEREncodedByteArray());
    }

    public void setAXDRDateTimeAttr(AXDRDateTime dateTime) throws IOException {
    	write(ClockAttributes.TIME, dateTime.getBEREncodedByteArray());
    }

    public void setDateTime(Date dateTime) throws IOException {
        setAXDRDateTimeAttr(new AXDRDateTime(dateTime));
    }

    public void setDateTime(Calendar dateTime) throws IOException {
        setDateTime(dateTime.getTime());
    }

    public AbstractDataType getTimeAttr() throws IOException {
        return AXDRDecoder.decode(getResponseData(ClockAttributes.TIME));
    }

    /**
     * Adjusts the time of the clock to the preset time.
     *
     * This method is used in conjunction with the {@link #presetAdjustingTime(com.energyict.dlms.axrdencoding.util.AXDRDateTime dateTime)}  method.
     * If the meter’s time lies between validity_interval_start and validity_interval_end, then the time is set to preset_time
     *
     * @throws java.io.IOException                    If an IO error occurs during the method invocation.
     */
    public final void adjustToPresetTime() throws IOException {
    	if (getObjectReference().isLNReference()) {
    		this.invoke(METHODID_ADJUST_TO_PRESET_TIME, new Integer8(0).getBEREncodedByteArray());
		} else {
    		this.invoke(METHODID_ADJUST_TO_PRESET_TIME_SN, new Integer8(0).getBEREncodedByteArray());
    	}
    }

    /**
     * Method to preset the time to a new value (preset_time) and defines a validity_interval within which the new time can be activated.
     *
     * This method is used in conjunction with the {@link #adjustToPresetTime()}  method.
     *
     * @param dateTime  The time to preset
     *
     * @throws java.io.IOException     If an IO error occurs during the method invocation.
     */
    public final void presetAdjustingTime(AXDRDateTime dateTime) throws IOException {
    	presetAdjustingTime(dateTime, null, null);
    }

    /**
     * Method to preset the time to a new value (preset_time) and defines a validity_interval within which the new time can be activated.
     *
     * This method is used in conjunction with the {@link #adjustToPresetTime()}  method.
     * If the meter’s time lies between validity_interval_start and validity_interval_end, then the time is set to preset_time
     *
     * @param presetDateTime  The time to preset
     * @param validityIntervalStartDateTime The start time of the validity interval - by default, when null, the current time of the clock - 2 min will be used.
     * @param validityIntervalEndDateTime The end time of the validity interval - by default, when null, the current time of the clock + 2 min will be used.
     *
     * @throws java.io.IOException     If an IO error occurs during the method invocation.
     */
    public final void presetAdjustingTime(AXDRDateTime presetDateTime, AXDRDateTime validityIntervalStartDateTime, AXDRDateTime validityIntervalEndDateTime) throws IOException {
        Calendar currentDeviceTimeCalendar = getAXDRDateTime().getValue();
        if (validityIntervalStartDateTime == null) {
            Calendar validityIntervalStartDateTimeCalendar = (Calendar) currentDeviceTimeCalendar.clone();
            validityIntervalStartDateTimeCalendar.add(Calendar.MINUTE, -2);
            validityIntervalStartDateTime = new AXDRDateTime(validityIntervalStartDateTimeCalendar);
        }
        if (validityIntervalEndDateTime == null) {
            Calendar validityIntervalEndDateTimeCalendar = (Calendar) currentDeviceTimeCalendar.clone();
            validityIntervalEndDateTimeCalendar.add(Calendar.MINUTE, 2);
            validityIntervalEndDateTime = new AXDRDateTime(validityIntervalEndDateTimeCalendar);
        }

        Structure adjustingTimeStructure = new Structure(presetDateTime, validityIntervalStartDateTime, validityIntervalEndDateTime);
        System.out.println(adjustingTimeStructure);
        if (getObjectReference().isLNReference()) {
            this.invoke(METHODID_PRESET_ADJUSTING_TIME, adjustingTimeStructure.getBEREncodedByteArray());
        } else {
            this.invoke(METHODID_PRESET_ADJUSTING_TIME_SN, adjustingTimeStructure.getBEREncodedByteArray());
        }
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
     * @throws java.io.IOException                    If an IO error occurs during the method invocation.
     * @throws IllegalArgumentException    In case the offset does not fall between the specified boundaries (-900s <= offset <= 900s).
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
