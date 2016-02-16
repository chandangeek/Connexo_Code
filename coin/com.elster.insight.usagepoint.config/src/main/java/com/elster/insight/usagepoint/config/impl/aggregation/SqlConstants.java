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
        ID("id"),

        /**
         * The timestamp of a TimeSeries interval.
         * @see com.elster.jupiter.ids.TimeSeriesEntry#getTimeStamp()
         */
        TIMESTAMP("timestamp"),

        /**
         * The version of a TimeSeries interval.
         * @see com.elster.jupiter.ids.TimeSeriesEntry#getVersion()
         */
        VERSIONCOUNT("versioncount"),

        /**
         * The version of a TimeSeries interval.
         * @see com.elster.jupiter.ids.TimeSeriesEntry#getRecordDateTime()
         */
        RECORDTIME("recordtime"),

        /**
         * The local date of a TimeSeries interval
         */
        LOCALDATE("localdate"),

        /**
         * The version of a TimeSeries interval.
         * @see com.elster.jupiter.ids.TimeSeriesEntry#getLong(int)
         */
        PROCESSSTATUS("processStatus"),

        /**
         * The value of a TimeSeries interval.
         * @see com.elster.jupiter.ids.TimeSeriesEntry#getValues()
         */
        VALUE("value");


        private final String sqlName;

        TimeSeriesColumnNames(String sqlName) {
            this.sqlName = sqlName;
        }

        String sqlName() {
            return this.sqlName;
        }

        static String[] names() {
            String[] names = new String[values().length];
            int i = 0;
            for (TimeSeriesColumnNames columnNames : values()) {
                names[i] = columnNames.sqlName();
                i++;
            }
            return names;
        }
    }

    // Hide constructor for class that only contains definitions of constants
    private SqlConstants() {}

}