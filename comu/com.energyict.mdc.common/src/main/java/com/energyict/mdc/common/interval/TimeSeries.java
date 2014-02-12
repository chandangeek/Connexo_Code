package com.energyict.mdc.common.interval;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.common.Unit;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * TimeSeriesField captures the simularities between a channel, a virtual meter
 * and a virtual meter field.
 *
 * @author Karel
 */

public interface TimeSeries {

    int DSTAMPTSTAMP = 1;
    int UTCSTAMP = 2;
    int UTCDATE = 3;
    int UTCGENERATION = 4;

    /**
     * Returns the receiver's parent's name, if part of a parent object.
     *
     * @return the parent's name.
     */
    String getParentName();

    /**
     * Returns the intervalData for the given period.
     *
     * @param from start date and time.
     * @param to   end date and time.
     * @return a List of IntervalRecord objects.
     * @deprecated use getIntervalData(Date from , Date to)
     */
    @Deprecated
    List<IntervalRecord> getIntervalData(DateTime from, DateTime to);

    /**
     * Returns the intervalData for the given period.
     *
     * @param from start date.
     * @param to   end date.
     * @return a List of IntervalRecord objects.
     */
    List<IntervalRecord> getIntervalData(Date from, Date to);

    /**
     * Returns the intervalData
     *
     * @param period to collect
     * @return a List of IntervalRecord objects.
     */
    List<IntervalRecord> getIntervalData(Interval period);

    /**
     * Returns the intervalData for the given period.
     *
     * @param from              start date.
     * @param to                end date.
     * @param includeValidation indiates if validation status should be fetched.
     * @return a List of IntervalRecord objects.
     */
    List<IntervalRecord> getIntervalData(Date from, Date to, boolean includeValidation);

    /**
     * Returns the intervalData that has changed since the specified date.
     *
     * @param since changed since date.
     * @return a List of IntervalRecord objects.
     */
    List<IntervalRecord> getChangedIntervalData(Date since);

    /**
     * Returns the intervalData that has changed since the specified date, for
     * the given period
     *
     * @param since changed since date.
     * @param from  period start date
     * @param to    period end date
     * @return a List of IntervalRecord objects.
     */
    List<IntervalRecord> getChangedIntervalData(Date since, Date from, Date to);

    /**
     * Returns the number of fields in the load profile.
     *
     * @return the number of fields.
     */
    int getFieldCount();

    /**
     * Returns the field names.
     *
     * @return an Array of name strings.
     */
    String[] getFieldNames();

    /**
     * Returns the field units.
     *
     * @return an Array of unit strings.
     */
    Unit[] getFieldUnits();

    /**
     * Returns the integration interval in seconds.
     *
     * @return the integration interval.
     */
    int getIntervalInSeconds();

    //return -1 if not exist

    /**
     * Returns the field index for the field with the given name
     *
     * @param fieldName the field name
     * @return the zero based field index or -1 if the field does not exist.
     */
    int getIndexFor(String fieldName);

    /**
     * Returns the last validated date.
     *
     * @return date.
     */
    Date getValidationLastValidDate();

    /**
     * returns the receiver's name
     *
     * @return the name
     */
    String getName();

    /**
     * Returns the receiver external name.
     *
     * @return the name.
     */
    String getExternalName();

    /**
     * Tests if the receiver is a virtual meter
     *
     * @return true if the receiver is a virtual meter
     */
    boolean isVirtualMeter();

    /**
     * Tests if the receiver is a channel
     *
     * @return returns true if the receiver is a channel
     */
    boolean isChannel();

    /**
     * Returns the sub timeseries with the given name
     *
     * @param name the field name
     * @return the TimeSeries with the given field name
     */
    SingleTimeSeries getTimeSeries(String name);

    /**
     * Returns the sub timeSeries for the given field index
     *
     * @param index the zero based index
     * @return the TimeSeries with the given index
     */
    SingleTimeSeries getTimeSeries(int index);

    /**
     * Returns the Phenomenon for the field with the given name
     *
     * @param name the field name.
     * @return the Phenomenon for the given field name.
     */
    Phenomenon getPhenomenon(String name);

    /**
     * Returns the phenomenon for the given field index.
     *
     * @param index the zero based field index
     * @return the phenomenon for the field with the given index.
     */
    Phenomenon getPhenomenon(int index);

    /**
     * Returns the receiver's time zone.
     *
     * @return the time zone.
     */
    TimeZone getTimeZone();

    /**
     * Test if the receiver maintains interval state information
     *
     * @return true if interval state information is maintained
     */
    boolean hasIntervalState();

    /**
     * Tests if the receiver maintains a last modification timestamp
     *
     * @return true if the last modification timestamp is maintained
     */
    boolean hasModDate();

    /**
     * Returns the end time of the last interval checked by the validation
     * process.
     *
     * @return the last checked interval's end time.
     */
    Date getValidationLastCheckedValue();

    /**
     * Returns the receiver's last reading Date. This is the date of the last
     * interval as reported by the data collection process (comserver or
     * import). This value is used by the validation process.
     *
     * @return the last reading date.
     */
    Date getLastReading();

    /**
     * Returns interval data
     *
     * @param from start of period to retrieve
     * @param to   end of period to retrieve
     * @param when date when intervaldata was current
     * @return a List of IntervalRecord objects
     */
    List<IntervalRecord> getIntervalDataAt(Date from, Date to, Date when);

    /**
     * Returns the timeseries interval record for the given date
     *
     * @param date the date to get the timeseries interval record for
     * @return the timeseries interval record for the given date
     */
    IntervalRecord getIntervalRecord(Date date);

    /**
     * @return the name of the associated view
     * @deprecated use getViewBuilder()
     */
    @Deprecated
    String getViewName();

    /**
     * Returns the number of intervals for this {@link TimeSeries} between the
     * given dates
     *
     * @param period the period to count all intervals in
     * @return the number of intervals for this {@link TimeSeries} between the
     *         given dates
     */
    int getIntervalCount(Interval period);

    /**
     * Returns the table used by this TimeSeries Not part of the API
     *
     * @return the view or table name
     */
    String getCanonicalName();

    int getDateFormat();

    SqlBuilder getUnionViewBuilder(Date from, Date to, Date when, boolean needsWhere) throws BusinessException;

    SqlBuilder getViewBuilder(Date from, Date to, Date when) throws BusinessException;

    SqlBuilder getViewBuilder(Date from, Date to) throws BusinessException;

    SqlBuilder getViewBuilder() throws BusinessException;

}
