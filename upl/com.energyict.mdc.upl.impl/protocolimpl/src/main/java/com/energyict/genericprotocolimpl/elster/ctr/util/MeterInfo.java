package com.energyict.genericprotocolimpl.elster.ctr.util;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.GprsRequestFactory;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.info.ConverterType;
import com.energyict.genericprotocolimpl.elster.ctr.info.MeterType;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.DateAndTimeCategory;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.NackStructure;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.ReferenceDate;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.WriteDataBlock;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 14-okt-2010
 * Time: 16:31:45
 */
public class MeterInfo extends AbstractUtilObject {

    private static final Unit METER_CALIBER_UNIT = Unit.get("m3/h");
    private static final Unit PULSE_WEIGHT_UNIT = Unit.get("m3");

    private static final String CLOCK_OBJECT_ID = "8.0.0";
    private static final String MTU_SERIAL_OBJECT_ID = "9.2.0";
    private static final String CONVERTOR_MASTER_RECORD_OBJECT_ID = "C.2.1";
    private static final String METER_MASTER_RECORD_OBJECT_ID = "C.2.0";
    private static final String VM_PULSE_WEIGHT = "13.7.1";
    private static final String VBS_PULSE_WEIGHT = "13.7.2";

    private static final String[] OBJECTS_TO_REQUEST = new String[]{
            CLOCK_OBJECT_ID,
            MTU_SERIAL_OBJECT_ID,
            CONVERTOR_MASTER_RECORD_OBJECT_ID,
            METER_MASTER_RECORD_OBJECT_ID,
            VM_PULSE_WEIGHT,
            VBS_PULSE_WEIGHT
    };

    private TimeZone timeZone;
    private List<AbstractCTRObject> ctrObjectList;
    private long time;
    private int wdbCounter = 0x05;

    public MeterInfo(GprsRequestFactory requestFactory, Logger logger, TimeZone timeZone) {
        super(requestFactory, logger);
        if (timeZone == null) {
            this.timeZone = TimeZone.getDefault();
            getLogger().warning("No timezone given. Using default timeZone: [" + this.timeZone.getID() + "]");
        } else {
            this.timeZone = timeZone;
        }
    }

    public MeterInfo(GprsRequestFactory requestFactory, Logger logger) {
        super(requestFactory, logger);
        this.timeZone = TimeZone.getDefault();
        getLogger().warning("No timezone given. Using default timeZone: [" + this.timeZone.getID() + "]");
    }

    /**
     * Gets the meter time.
     *
     * @return the meter time.
     * @throws CTRException
     */
    public Date getTime() throws CTRException {
        try {
            return getDateFromObject(getMeterInfoObjects().get(0));
        } catch (CTRException e) {
            throw new CTRException("Unable to read Clock", e);
        }
    }

    /**
     * Sends an execute command to the meter, to set the time.
     *
     * @param referenceDate
     * @param cal
     * @return Ack or Nack Structure
     * @throws CTRException
     */
    private void setTime(Date referenceDate, Calendar cal) throws CTRException {

        int mode = 0;
        int year = cal.get(Calendar.YEAR) - 2000;
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 2) % 7;
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);
        int seconds = cal.get(Calendar.SECOND);
        int timeZoneOffset = cal.getTimeZone().getRawOffset() / 3600000;
        int inDST = cal.getTimeZone().inDaylightTime(cal.getTime()) ? 1 : 0;

/*
        System.out.println(referenceDate);
        System.out.println(mode);
        System.out.println(year);
        System.out.println(month);
        System.out.println(day);
        System.out.println(dayOfWeek);
        System.out.println(hour);
        System.out.println(minutes);
        System.out.println(seconds);
        System.out.println(timeZoneOffset);
        System.out.println(inDST);
*/

        byte[] data = new byte[10];
        data[0] = (byte) mode;
        data[1] = (byte) year;
        data[2] = (byte) month;
        data[3] = (byte) day;
        data[4] = (byte) dayOfWeek;
        data[5] = (byte) hour;
        data[6] = (byte) minutes;
        data[7] = (byte) seconds;
        data[8] = (byte) timeZoneOffset;
        data[9] = (byte) inDST;
        WriteDataBlock wdb = new WriteDataBlock(wdbCounter++);
        ReferenceDate refDate = new ReferenceDate().parse(referenceDate, timeZone);
        refDate.addOneDay();

        try {
            Data ackOrNack = getRequestFactory().executeRequest(refDate, wdb, new CTRObjectID("11.0.1"), data);
            if (ackOrNack instanceof NackStructure) {
                throw new CTRException("Unable to set the clock to ["+cal.getTime()+"]. Received NACK.");
            }
        } catch (CTRException e) {
            throw new CTRException("Unable to set the clock to ["+cal.getTime()+"]. " + e.getMessage());
        }
    }

    /**
     * Sends an execute command to the meter, to set the time.
     *
     * @param time: the desired time (date object)
     * @return Ack or Nack Structure
     * @throws CTRException
     */
    public void setTime(Date time) throws CTRException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        setTime(getWriteTimeRefDate(), cal);
    }

    private Date getWriteTimeRefDate() {
        try {
            return new Date(getTime().getTime() + 24*3600*1000);
        } catch (CTRException e) {
            return new Date();
        }
    }

    /**
     * @return the meter's serial number.
     * @throws CTRException
     */
    public String getMTUSerialNumber() {
        try {
            if (getMeterInfoObjects().size() > 1) {
                AbstractCTRObject ctrObject = getMeterInfoObjects().get(1);
                CTRAbstractValue[] values = ctrObject.getValue();
                if ((values != null) && (values.length > 0)) {
                    Object value = values[0].getValue();
                    if (value != null) {
                        return value.toString();
                    }
                }
            }
            getLogger().severe("Unable to read MTU SerialNumber. Returned register list was empty or object was null.");
            return null;
        } catch (CTRException e) {
            getLogger().severe("Unable to read MTU SerialNumber: " + e.getMessage());
            return null;
        }
    }

    /**
     * Read the convertors serial number from the mtu
     *
     * @return the converter's serial number.
     */
    public String getConverterSerialNumber() {
        try {
            if (getMeterInfoObjects().size() > 2) {
                AbstractCTRObject ctrObject = getMeterInfoObjects().get(2);
                if (ctrObject != null) {
                    CTRAbstractValue[] values = ctrObject.getValue();
                    if ((values != null) && (values.length > 1)) {
                        Object value = values[1].getValue();
                        if (value != null) {
                            return value.toString();
                        }
                    }
                }
            }
            getLogger().severe("Unable to read Converter SerialNumber. Returned register list was empty or object was null.");
            return null;
        } catch (CTRException e) {
            getLogger().severe("Unable to read Converter SerialNumber: " + e.getMessage());
            return null;
        }
    }

    /**
     * Read the convertor type from the mtu
     *
     * @return the convertor type
     */
    public ConverterType getConverterType() {
        try {
            if (getMeterInfoObjects().size() > 2) {
                AbstractCTRObject ctrObject = getMeterInfoObjects().get(2);
                if (ctrObject != null) {
                    CTRAbstractValue[] values = ctrObject.getValue();
                    if ((values != null) && (values.length > 0)) {
                        Object value = values[0].getValue();
                        if ((value != null) && (value instanceof String)) {
                            return ConverterType.fromString((String) value);
                        }
                    }
                }
            }
            getLogger().severe("Unable to read the convertor type. Returned register list was empty or object was null.");
            return ConverterType.UNKNOWN;
        } catch (CTRException e) {
            getLogger().severe("Unable to read the convertor type: " + e.getMessage());
            return ConverterType.UNKNOWN;
        }
    }

    /**
     * Read the meter type from the mtu
     *
     * @return the meter type
     */
    public MeterType getMeterType() {
        try {
            if (getMeterInfoObjects().size() > 3) {
                AbstractCTRObject ctrObject = getMeterInfoObjects().get(3);
                if (ctrObject != null) {
                    CTRAbstractValue[] values = ctrObject.getValue();
                    if ((values != null) && (values.length > 0)) {
                        Object value = values[0].getValue();
                        if ((value != null) && (value instanceof String)) {
                            return MeterType.fromString((String) value);
                        }
                    }
                }
            }
            getLogger().severe("Unable to read the meter type. Returned register list was empty or object was null.");
            return MeterType.UNKNOWN;
        } catch (CTRException e) {
            getLogger().severe("Unable to read the meter type: " + e.getMessage());
            return MeterType.UNKNOWN;
        }
    }

    /**
     * Read the meter caliber from the mtu
     *
     * @return the meter caliber
     */
    public Quantity getMeterCaliber() {
        try {
            if (getMeterInfoObjects().size() > 3) {
                AbstractCTRObject ctrObject = getMeterInfoObjects().get(3);
                if (ctrObject != null) {
                    CTRAbstractValue[] values = ctrObject.getValue();
                    if ((values != null) && (values.length > 1)) {
                        Object value = values[1].getValue();
                        if ((value != null) && (value instanceof Number)) {
                            BigDecimal decimal = (BigDecimal) value;
                            int kmoltFactor = ctrObject.getQlf().getKmoltFactor();
                            decimal = decimal.movePointRight(kmoltFactor);
                            return new Quantity(decimal, METER_CALIBER_UNIT);
                        }
                    }
                }
            }
            getLogger().severe("Unable to read the meter caliber. Returned register list was empty or object was null.");
            return new Quantity(0, METER_CALIBER_UNIT);
        } catch (CTRException e) {
            getLogger().severe("Unable to read the meter caliber: " + e.getMessage());
            return new Quantity(0, METER_CALIBER_UNIT);
        }
    }

    /**
     * Read the pulseWeightVm from the mtu
     *
     * @return the pulseWeightVm
     */
    public Quantity getPulseWeightVm() {
        try {
            if (getMeterInfoObjects().size() > 4) {
                AbstractCTRObject ctrObject = getMeterInfoObjects().get(4);
                if (ctrObject != null) {
                    CTRAbstractValue[] values = ctrObject.getValue();
                    if ((values != null) && (values.length > 0)) {
                        Object value = values[0].getValue();
                        if ((value != null) && (value instanceof Number)) {
                            BigDecimal decimal = (BigDecimal) value;
                            int kmoltFactor = ctrObject.getQlf().getKmoltFactor();
                            decimal = decimal.movePointRight(kmoltFactor);
                            return new Quantity(decimal, PULSE_WEIGHT_UNIT);
                        }
                    }
                }
            }
            getLogger().severe("Unable to read the pulseWeightVm. Returned register list was empty or object was null.");
            return new Quantity(0, PULSE_WEIGHT_UNIT);
        } catch (CTRException e) {
            getLogger().severe("Unable to read the pulseWeightVm: " + e.getMessage());
            return new Quantity(0, PULSE_WEIGHT_UNIT);
        }
    }

    /**
     * Read the pulseWeightVbs from the mtu
     *
     * @return the meter caliber
     */
    public Quantity getPulseWeightVbs() {
        try {
            if (getMeterInfoObjects().size() > 5) {
                AbstractCTRObject ctrObject = getMeterInfoObjects().get(5);
                if (ctrObject != null) {
                    CTRAbstractValue[] values = ctrObject.getValue();
                    if ((values != null) && (values.length > 0)) {
                        Object value = values[0].getValue();
                        if ((value != null) && (value instanceof Number)) {
                            BigDecimal decimal = (BigDecimal) value;
                            int kmoltFactor = ctrObject.getQlf().getKmoltFactor();
                            decimal = decimal.movePointRight(kmoltFactor);
                            return new Quantity(decimal, PULSE_WEIGHT_UNIT);
                        }
                    }
                }
            }
            getLogger().severe("Unable to read the pulseWeightVbs. Returned register list was empty or object was null.");
            return new Quantity(0, PULSE_WEIGHT_UNIT);
        } catch (CTRException e) {
            getLogger().severe("Unable to read the pulseWeightVbs: " + e.getMessage());
            return new Quantity(0, PULSE_WEIGHT_UNIT);
        }
    }

    /**
     * Parses an object and gets a date time object from it
     *
     * @param object
     * @return date time
     * @throws CTRException
     */
    private Date getDateFromObject(AbstractCTRObject object) throws CTRException {
        Calendar cal = Calendar.getInstance(timeZone);
        if (!(object instanceof DateAndTimeCategory)) {
            throw new CTRException("Expected DateAndTimeCategory object!");
        } else {
            DateAndTimeCategory dateAndTime = (DateAndTimeCategory) object;
            CTRAbstractValue<BigDecimal>[] values = dateAndTime.getValue();
            int ptr = 0;
            int year = values[ptr++].getValue().intValue() + 2000;
            int month = values[ptr++].getValue().intValue() - 1;
            int day = values[ptr++].getValue().intValue();
            ptr++; // Day of week
            int hour = values[ptr++].getValue().intValue();
            int min = values[ptr++].getValue().intValue();
            int sec = values[ptr++].getValue().intValue();

            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, min);
            cal.set(Calendar.SECOND, sec);
            cal.set(Calendar.MILLISECOND, 0);
        }
        cal.add(Calendar.MILLISECOND, (int) (System.currentTimeMillis() - time));

        return cal.getTime();
    }

    /**
     * Gets the meter time and the converter and the meter's serial number (in one request).
     * Caches the result.
     *
     * @return object list containing the meter time and the serial numbers.
     * @throws CTRException
     */
    private List<AbstractCTRObject> getMeterInfoObjects() throws CTRException {
        if (ctrObjectList == null) {
            AttributeType attributeType = new AttributeType(0x03);
            ctrObjectList = getRequestFactory().queryRegisters(attributeType, OBJECTS_TO_REQUEST);
            time = System.currentTimeMillis();
        }
        return ctrObjectList;
    }
}
