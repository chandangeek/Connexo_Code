package com.energyict.genericprotocolimpl.elster.ctr.profile;

import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.Qualifier;
import com.energyict.genericprotocolimpl.elster.ctr.structure.Trace_CQueryResponseStructure;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.PeriodTrace_C;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.ReferenceDate;
import com.energyict.protocol.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 26-jan-2011
 * Time: 9:02:14
 */
public class TraceCProfileParser {

    private static final int START_OF_GASDAY = 6;

    private final Trace_CQueryResponseStructure response;
    private final TimeZone deviceTimeZone;
    private Calendar fromCalendar = null;
    private Calendar toCalendar = null;

    /**
     * This class parses the content from a Trace_CQueryResponseStructure to a list of intervalData items
     *
     * @param rawPacket      The raw packet, containing the Trace_CQueryResponseStructure, received from the meter
     * @param deviceTimeZone The device timezone, used to calculate the timeStamps of the intervals
     */
    public TraceCProfileParser(byte[] rawPacket, TimeZone deviceTimeZone) {
        if (rawPacket == null) {
            throw new IllegalArgumentException("Parameter [Trace_CQueryResponseStructure response] cannot be null!");
        }


        try {
            GPRSFrame frame = new GPRSFrame().parse(rawPacket, 0);
            if (!(frame.getData() instanceof Trace_CQueryResponseStructure)) {
                throw new IllegalArgumentException("Parameter [byte[] rawPacket] does not contain an Trace_CQueryResponseStructure!");
            } else {
                this.response = (Trace_CQueryResponseStructure) frame.getData();
            }
        } catch (CTRParsingException e) {
            throw new IllegalArgumentException("Parameter [byte[] rawPacket] contains invalid data! " + e.getMessage());
        }

        this.deviceTimeZone = (deviceTimeZone == null ? TimeZone.getDefault() : deviceTimeZone);
    }

    /**
     * This class parses the content from a Trace_CQueryResponseStructure to a list of intervalData items
     *
     * @param response       The Trace_CQueryResponseStructure, received from the meter
     * @param deviceTimeZone The device timezone, used to calculate the timeStamps of the intervals
     */
    public TraceCProfileParser(Trace_CQueryResponseStructure response, TimeZone deviceTimeZone) {
        if (response == null) {
            throw new IllegalArgumentException("Parameter [Trace_CQueryResponseStructure response] cannot be null!");
        }
        this.response = response;
        this.deviceTimeZone = (deviceTimeZone == null ? TimeZone.getDefault() : deviceTimeZone);
    }

    /**
     * Get all the profile data entries this response contains
     *
     * @return
     */
    public List<IntervalData> getIntervalData() {
        List<IntervalData> intervalData = new ArrayList<IntervalData>();
        for (int i = 0; i < getMaxNumberOfEntries(); i++) {
            List<IntervalValue> values = new ArrayList<IntervalValue>();
            values.add(new IntervalValue(getDataValues(i), getProtocolStatus(i), getEIStatus(i)));
            IntervalData data = new IntervalData(getTimeStamp(i).getTime(), getEIStatus(i), getProtocolStatus(i), 0, values);
            intervalData.add(data);
        }
        return intervalData;
    }

    /**
     * Get all the profile data entries this response contains
     *
     * @return
     */
    public List<IntervalData> getIntervalData(Calendar from, Calendar to) {
        List<IntervalData> filtered = new ArrayList<IntervalData>();
        List<IntervalData> all = getIntervalData();
        for (IntervalData intervalData : all) {
            long endTime = intervalData.getEndTime().getTime();
            boolean fromTimeOk = (from == null) || (endTime > from.getTimeInMillis());
            boolean toTimeOk = (to == null) || endTime <= to.getTimeInMillis();
            if (fromTimeOk && toTimeOk) {
                filtered.add(intervalData);
            }
        }
        return filtered;
    }

    /**
     * Get the value for a given valueIndex
     *
     * @param valueIndex
     * @return
     */
    private BigDecimal getDataValues(int valueIndex) {
        checkValueIndex(valueIndex);
        if ((getEIStatus(valueIndex) & IntervalStateBits.CORRUPTED) == 0) {
            AbstractCTRObject object = response.getTraceData().get(valueIndex);
            Object objectValue = object.getValue(0).getValue();
            if (objectValue instanceof BigDecimal) {
                BigDecimal decimal = (BigDecimal) objectValue;
                decimal = decimal.movePointRight(object.getQlf().getKmoltFactor());
                return decimal;
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Get the translated interval status code for a given valueIndex
     *
     * @param valueIndex
     * @return
     */
    private int getEIStatus(int valueIndex) {
        checkValueIndex(valueIndex);
        Qualifier qlf = response.getTraceData().get(valueIndex).getQlf();
        if (qlf.isInvalidMeasurement()) {
            return IntervalStateBits.CORRUPTED;
        } else if (qlf.isSubjectToMaintenance()) {
            return IntervalStateBits.OTHER;
        } else if (qlf.isReservedVal()) {
            return IntervalStateBits.OTHER;
        } else {
            return IntervalStateBits.OK;
        }
    }

    /**
     * Get the raw interval status code for a given valueIndex as received from the device
     *
     * @param valueIndex
     * @return
     */
    private int getProtocolStatus(int valueIndex) {
        return response.getTraceData().get(valueIndex).getQlf().getQlf();
    }

    /**
     * Get the endTime of the interval for a given valueIndex
     *
     * @param valueIndex
     * @return
     */
    private Calendar getTimeStamp(int valueIndex) {
        checkValueIndex(valueIndex);
        Calendar timeStamp = (Calendar) getFromCalendar().clone();
        timeStamp.add(Calendar.SECOND, getIntervalInSeconds() * valueIndex);
        return timeStamp;
    }

    /**
     * Get the endTime of the first interval
     *
     * @return
     */
    public Calendar getFromCalendar() {
        if (fromCalendar == null) {
            fromCalendar = response.getDate().getCalendar(deviceTimeZone);
            fromCalendar.add(Calendar.HOUR_OF_DAY, response.getEndOfDayTime().getIntValue());
            if (getPeriod().isHourly()) {
                fromCalendar.add(Calendar.SECOND, getIntervalInSeconds());
            } else if (getPeriod().isDaily()) {
                fromCalendar.add(Calendar.SECOND, getIntervalInSeconds());
                fromCalendar.add(Calendar.SECOND, -(getIntervalInSeconds() * getMaxValueIndex()));
            } else if (getPeriod().isMonthly()) {
                // TODO: Aanvullen
                throw new IllegalArgumentException("Invalid period: " + getPeriod() + ". Monthly periods not yet supported.");
            } else {
                throw new IllegalArgumentException("Invalid period: " + getPeriod());
            }
        }
        return fromCalendar;
    }

    /**
     * Get the endTime of the last interval
     *
     * @return
     */
    public Calendar getToCalendar() {
        if (toCalendar == null) {
            toCalendar = (Calendar) getFromCalendar().clone();
            toCalendar.add(Calendar.SECOND, getIntervalInSeconds() * getMaxValueIndex());
        }
        return toCalendar;
    }

    /**
     * Check if a givan index points to a valid trace_c entry
     *
     * @param valueIndex
     */
    private void checkValueIndex(int valueIndex) {
        if ((valueIndex < 0) || (valueIndex > getMaxValueIndex())) {
            throw new IllegalArgumentException("Parameter valueIndex should be in range of [0-" + getMaxValueIndex() + "] but was " + valueIndex + ".");
        }
    }

    /**
     * Get the profileInterval from the Trace_C response
     *
     * @return
     */
    private int getIntervalInSeconds() {
        return getPeriod().getIntervalInSeconds();
    }

    /**
     * Get the maximum valueIndex, considering we're using a 0 based index.
     * The low the index, the older the interval timestamp
     *
     * @return
     */
    private int getMaxValueIndex() {
        return getMaxNumberOfEntries() - 1;
    }

    /**
     * Get the maximum number of entries a Trace_C object can contain.
     * This depends on the type of data thats requested from the meter
     *
     * @return
     */
    private int getMaxNumberOfEntries() {
        return getPeriod().getTraceCIntervalCount();
    }

    /**
     * Getter for the period object inside the response
     *
     * @return
     */
    private PeriodTrace_C getPeriod() {
        return response.getPeriod();
    }

    public static ReferenceDate calcRefDate(Calendar from, PeriodTrace_C period) {
        ReferenceDate date = new ReferenceDate();
        if (period.isHourly()) {
            date.parse(getStartOfGasDay(from));
        } else if (period.isDaily()) {
            Calendar startDay = getStartOfGasDay(from);
            startDay.add(Calendar.DAY_OF_YEAR, period.getTraceCIntervalCount() - 1);
            date.parse(startDay);
        } else if (period.isMonthly()) {
            date.parse(getStartOfGasDay(from));
        }
        return date;
    }

    public static Calendar getStartOfGasDay(Calendar from) {
        Calendar startOfGasDay = (Calendar) from.clone();
        if (from.get(Calendar.HOUR_OF_DAY) < START_OF_GASDAY) {
            startOfGasDay.add(Calendar.DAY_OF_YEAR, -1);
        }
        startOfGasDay.set(Calendar.HOUR_OF_DAY, START_OF_GASDAY);
        startOfGasDay.set(Calendar.MINUTE, 0);
        startOfGasDay.set(Calendar.SECOND, 0);
        startOfGasDay.set(Calendar.MILLISECOND, 0);
        return startOfGasDay;
    }

}
