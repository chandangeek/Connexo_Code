package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.impl.RecordSpecs;
import com.elster.jupiter.util.sql.SqlBuilder;

/**
 * Defines a set of constants that will be used in the SQL
 * that will be generated to do data aggregation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-11 (10:12)
 */
final class SqlConstants {

    /**
     * The value that will be used as the identifier of a virtual TimeSeries,
     * i.e. one that is generated on the fly in a WITH statement,
     * that does not really have a database identifier because it is virtual.
     */
    static final String VIRTUAL_TIMESERIES_ID = "-1";

    /**
     * The value that will be used as the version count of a virtual TimeSeries,
     * i.e. one that is generated on the fly in a WITH statement.
     * Since such a TimeSeries is not really persistent, it does not have
     * a database version count.
     */
    static final String VIRTUAL_VERSION_COUNT = "0";

    /**
     * The value that will be used as the recordTime of a virtual TimeSeries,
     * i.e. one that is generated on the fly in a WITH statement.
     * Since such a TimeSeries is not really persistent, the instant in time
     * on which it was created in the database is not really known.
     */
    static final String VIRTUAL_RECORD_TIME = "0";

    /**
     * Constants for the column names that are used for
     * SQL constructs that return TimeSeries related data.
     */
    enum TimeSeriesColumnNames {
        /**
         * The identifier of the TimeSeries
         * @see com.elster.jupiter.ids.TimeSeries#getId()
         */
        ID("id", "TIMESERIESID") {
            @Override
            void appendAsDeliverableSelectValue(ServerExpressionNode expressionNode, SqlBuilder sqlBuilder) {
                sqlBuilder.append(VIRTUAL_TIMESERIES_ID);
            }
        },

        /**
         * The timestamp of a TimeSeries interval.
         * @see com.elster.jupiter.ids.TimeSeriesEntry#getTimeStamp()
         */
        TIMESTAMP("timestamp", "UTCSTAMP") {
            @Override
            void appendAsDeliverableSelectValue(ServerExpressionNode expressionNode, SqlBuilder sqlBuilder) {
                sqlBuilder.append(expressionNode.accept(new TimeStampFromExpressionNode()));
            }
        },

        /**
         * The version of a TimeSeries interval.
         * @see com.elster.jupiter.ids.TimeSeriesEntry#getVersion()
         */
        VERSIONCOUNT("versioncount", "VERSIONCOUNT") {
            @Override
            void appendAsDeliverableSelectValue(ServerExpressionNode expressionNode, SqlBuilder sqlBuilder) {
                sqlBuilder.append(VIRTUAL_VERSION_COUNT);
            }
        },

        /**
         * The version of a TimeSeries interval.
         * @see com.elster.jupiter.ids.TimeSeriesEntry#getRecordDateTime()
         */
        RECORDTIME("recordtime", "RECORDTIME") {
            @Override
            void appendAsDeliverableSelectValue(ServerExpressionNode expressionNode, SqlBuilder sqlBuilder) {
                sqlBuilder.append(VIRTUAL_RECORD_TIME);
            }
        },

        /**
         * The version of a TimeSeries interval.
         * @see com.elster.jupiter.ids.TimeSeriesEntry#getLong(int)
         */
        PROCESSSTATUS("processStatus", RecordSpecs.PROCESS_STATUS) {
            @Override
            void appendAsDeliverableSelectValue(ServerExpressionNode expressionNode, SqlBuilder sqlBuilder) {
                // Todo: use all_or construct to aggregate the process status flags
                sqlBuilder.append("0");
            }
        },

        /**
         * The value of a TimeSeries interval.
         * @see com.elster.jupiter.ids.TimeSeriesEntry#getValues()
         */
        VALUE("value", RecordSpecs.VALUE) {
            @Override
            void appendAsDeliverableSelectValue(ServerExpressionNode expressionNode, SqlBuilder sqlBuilder) {
                sqlBuilder.add(expressionNode.accept(new ExpressionNodeToSql()));
            }
        },

        /**
         * The local date of a TimeSeries interval
         */
        LOCALDATE("localdate", "LOCALDATE") {
            @Override
            void appendAsDeliverableSelectValue(ServerExpressionNode expressionNode, SqlBuilder sqlBuilder) {
                sqlBuilder.append(expressionNode.accept(new LocalDateFromExpressionNode()));
            }
        };

        private final String sqlName;
        private final String fieldSpecName;

        TimeSeriesColumnNames(String sqlName, String fieldSpecName) {
            this.sqlName = sqlName;
            this.fieldSpecName = fieldSpecName;
        }

        String sqlName() {
            return this.sqlName;
        }

        String fieldSpecName() {
            return this.fieldSpecName;
        }

        /**
         * Append the appropriate select value for the specified {@link ServerExpressionNode}
         * for all TimeSeriesColumnNames to the specified SqlBuilder.
         *
         * @param expressionNode The ServerExpressionNode
         * @param sqlBuilder The SqlBuilder
         */
        static void appendAllDeliverableSelectValues(ServerExpressionNode expressionNode, SqlBuilder sqlBuilder) {
            for (TimeSeriesColumnNames columnName : values()) {
                columnName.appendAsDeliverableSelectValue(expressionNode, sqlBuilder);
                if (columnName != LOCALDATE) {
                    sqlBuilder.append(", ");
                }
            }
        }

        abstract void appendAsDeliverableSelectValue(ServerExpressionNode expressionNode, SqlBuilder sqlBuilder);

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