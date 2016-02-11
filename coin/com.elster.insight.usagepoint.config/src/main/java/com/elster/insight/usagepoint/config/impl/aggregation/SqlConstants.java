package com.elster.insight.usagepoint.config.impl.aggregation;

/**
 * Defines a set of constants that will be used in the SQL
 * that will be generated to do data aggregation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-11 (10:12)
 */
final class SqlConstants {

    /**
     * Constants for the column names that are used for
     * SQL constructs that return TimeSeries related data.
     */
    enum TimeSeriesColumnNames {
        /**
         * The identifier of the TimeSeries
         * @see com.elster.jupiter.ids.TimeSeries#getId()
         */
        ID,

        /**
         * The value of a TimeSeries interval.
         * @see com.elster.jupiter.ids.TimeSeriesEntry#getValues()
         */
        VALUE,

        /**
         * The timestamp of a TimeSeries interval.
         * @see com.elster.jupiter.ids.TimeSeriesEntry#getTimeStamp()
         */
        TIMESTAMP,

        /**
         * The local date of a TimeSeries interval
         */
        LOCALDATE;
    }

    // Hide constructor for class that only contains definitions of constants
    private SqlConstants() {}

}