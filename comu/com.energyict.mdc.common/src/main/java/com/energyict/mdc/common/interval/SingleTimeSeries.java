package com.energyict.mdc.common.interval;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.Phenomenon;
import com.energyict.mdc.common.Quantity;

import java.util.Date;
import java.util.List;

/**
 * SingleTimeSeries captures the simularities between a channel and a virtual meter field.
 */

public interface SingleTimeSeries extends TimeSeries {

    /**
     * Returns the formula representation of the receiver
     * when used in a virtual meter formula
     *
     * @return the formula representation
     */
    public String formulaReference();

    /**
     * Returns the timeseries integrated consumption.
     * Note that this is not necessarily equal to getSum(Date from , Date to)
     * as this method using the timeseries interval
     * to convert from flow units to volume units if necessary
     * e.g. kW -> kWh , m3/h -> m3 , kvar -> kvarh ...
     * Also this method returns a zero quantity if no interval data
     * is available in the given period, while getSum returns null
     *
     * @param from Integration start date
     * @param to   Integration end date
     * @return the consumption
     */
    public Quantity getConsumption(Date from, Date to);

    /**
     * Returns the timeseries integrated consumption
     *
     * @param from    Integration start date
     * @param to      Integration end date
     * @param codeval Aggregate only the values with this calendar code
     * @return the consumption
     */
    public Quantity getConsumption(Date from, Date to, Integer codeval);

    /**
     * Returns the receiver's phenomenon
     *
     * @return the phenomenon
     */
    public Phenomenon getPhenomenon();

    /**
     * Returns the database column name this timeseries uses
     * Not part of the API
     *
     * @return the column name
     */
    public String getColumnName();

    /**
     * Returns the interval data for the given period.
     * Fills in any missing values.
     *
     * @param from period start date
     * @param to   period end date
     * @return a List of IntervalRecord objects
     */
    public List<IntervalRecord> getIntervalDataWithFill(Date from, Date to);

    /**
     * Returns the consumption in the time series
     *
     * @param period over which to calculate the maximum
     * @return the consumption
     */
    public Quantity getConsumption(Interval period);

    /**
     * Returns the minimum value in the time series
     *
     * @param period over which to calculate the maximum
     * @return the minimum value or null
     */
    public Quantity getMin(Interval period);

    /**
     * Returns the minimum value in the time series over the
     * given period
     *
     * @param from the period start date
     * @param to   the period end date
     * @return the minimum value or null
     */
    public Quantity getMin(Date from, Date to);


    /**
     * Returns the minimum value in the time series over the
     * given period
     *
     * @param from              the period start date
     * @param to                the period end date
     * @param intervalStateMask only include intervals whose intervalste do not have any of the mask bits set
     * @return the minimum value or null
     */
    public Quantity getMin(Date from, Date to, Integer intervalStateMask);

    /**
     * Returns the minimum value in the time series over the
     * given period
     *
     * @param from              the period start date
     * @param to                the period end date
     * @param codeval           Use only the values with this calendar code
     * @param intervalStateMask only include intervals whose intervalste do not have any of the mask bits set
     * @return the minimum value or null
     */
    public Quantity getMin(Date from, Date to, Integer codeval, Integer intervalStateMask);

    /**
     * Returns the maximum value in the time series
     *
     * @param period over which to calculate the maximum
     * @return the maximum value or null
     */
    public Quantity getMax(Interval period);

    /**
     * Returns the maximum value in the time series over the
     * given period
     *
     * @param from the period start date
     * @param to   the period end date
     * @return the maximum value
     */
    public Quantity getMax(Date from, Date to);

    /**
     * Returns the maximum value in the time series over the
     * given period
     *
     * @param from              the period start date
     * @param to                the period end date
     * @param intervalStateMask only include intervals whose
     *                          intervalste do not have any of the mask bits set
     * @return the maximum value or null
     */
    public Quantity getMax(Date from, Date to, Integer intervalStateMask);

    /**
     * Returns the timeseries maximum value
     *
     * @param from              period start date
     * @param to                period end date
     * @param codeval           Use only the values with this calendar code
     * @param intervalStateMask only include intervals whose
     *                          intervalste do not have any of the mask bits set
     * @return the maximum value or null
     */
    public Quantity getMax(Date from, Date to, Integer codeval, Integer intervalStateMask);

    /**
     * Returns the average value in the time series
     *
     * @param period over which to calculate the maximum
     * @return the average value or null
     */
    public Quantity getAvg(Interval period);

    /**
     * Returns the average value in the time series over the
     * given period
     *
     * @param from the period start date
     * @param to   the period end date
     * @return the average value or null
     */
    public Quantity getAvg(Date from, Date to);

    /**
     * Returns the average value in the time series over the given period with intervalState
     *
     * @param from              the period start date
     * @param to                the period stop date
     * @param intervalStateMask only include intervals whose intervalste do not have any of the mask bits set
     * @return the average value or null
     */
    public Quantity getAvg(Date from, Date to, Integer intervalStateMask);

    /**
     * Returns the average value in the time series over the given period with intervalState
     *
     * @param from              the period start date
     * @param to                the period stop date
     * @param codeval           Use only the values with this calendar code
     * @param intervalStateMask only include intervals whose intervalste do not have any of the mask bits set
     * @return the average value or null
     */
    public Quantity getAvg(Date from, Date to, Integer codeval, Integer intervalStateMask);

    /**
     * Returns the standard deviation in the time series
     *
     * @param period over which to calculate the maximum
     * @return the standard deviation or null
     */
    public Quantity getStdDev(Interval period);

    /**
     * Returns the standard deviation in the time series over the
     * given period
     *
     * @param from the period start date
     * @param to   the period end date
     * @return the standard deviation value or null
     */
    public Quantity getStdDev(Date from, Date to);

    /**
     * Returns the standard deviation in the time series over the
     * given period
     *
     * @param from              the period start date
     * @param to                the period end date
     * @param intervalStateMask only include intervals whose intervalste do not have any of the mask bits set
     * @return the standard deviation value or null
     */
    public Quantity getStdDev(Date from, Date to, Integer intervalStateMask);

    /**
     * Returns the standard deviation in the time series over the
     * given period
     *
     * @param from              the period start date
     * @param to                the period end date
     * @param codeval           Use only the values with this calendar code
     * @param intervalStateMask only include intervals whose intervalste do not have any of the mask bits set
     * @return the standard deviation value or null
     */
    public Quantity getStdDev(Date from, Date to, Integer codeval, Integer intervalStateMask);

    /**
     * Returns the sum in the time series
     * No unit conversion is performed
     *
     * @param period over which to calculate the sum
     * @return the consumption
     */
    public Quantity getSum(Interval period);

    /**
     * Returns the sum in the time series over the given period
     *
     * @param from the period start date
     * @param to   the period end date
     * @return the sum value or null
     */
    public Quantity getSum(Date from, Date to);

    /**
     * Returns the sum in the time series over the given period
     *
     * @param from              the period start date
     * @param to                the period end date
     * @param intervalStateMask only include intervals whose intervalste do not have any of the mask bits set
     * @return the sum value or null
     */
    public Quantity getSum(Date from, Date to, Integer intervalStateMask);

    /**
     * Returns the sum in the time series over the given period
     *
     * @param from              the period start date
     * @param to                the period end date
     * @param codeval           Use only the values with this calendar code
     * @param intervalStateMask only include intervals whose intervalste do not have any of the mask bits set
     * @return the sum value or null
     */
    public Quantity getSum(Date from, Date to, Integer codeval, Integer intervalStateMask);

    /**
     * Returns the top n values in the time series over the given period
     *
     * @param period the period
     * @param n      the number of values to take in consideration
     * @return a List of IntervalRecords with the top n values
     */
    public List<IntervalRecord> getTop(Interval period, int n);

    /**
     * Returns the top n values in the time series over the given period
     *
     * @param from the period start date
     * @param to   the period end date
     * @param n    the number of values to take in consideration
     * @return a List of IntervalRecords with the top n values
     */
    public List<IntervalRecord> getTop(Date from, Date to, int n);

    /**
     * Returns the top n values with code codeVal in the time series over the given period
     *
     * @param from    the period start date
     * @param to      the period end date
     * @param n       the number of values to take in consideration
     * @param codeVal code value to match
     * @return a List of IntervalRecords with the top n values
     */
    public List<IntervalRecord> getTop(Date from, Date to, int n, Integer codeVal);

}