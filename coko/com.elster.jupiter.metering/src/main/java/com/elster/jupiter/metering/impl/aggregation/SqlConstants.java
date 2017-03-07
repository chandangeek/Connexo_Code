/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.impl.RecordSpecs;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.Checks.is;

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
            void appendAsDeliverableSelectValue(Formula.Mode mode, ServerExpressionNode expressionNode, Optional<IntervalLength> expertIntervalLength, VirtualReadingType targetReadingType, SqlBuilder sqlBuilder) {
                sqlBuilder.append(VIRTUAL_TIMESERIES_ID);
            }

            @Override
            AggregationFunction aggregationFunctionFor(VirtualReadingType readingType) {
                return AggregationFunction.MIN;
            }

            @Override
            String aggregatedValue(VirtualReadingType sourceReadingType, VirtualReadingType readingType) {
                return this.fieldSpecName();
            }
        },

        /**
         * The timestamp of a TimeSeries interval.
         * @see com.elster.jupiter.ids.TimeSeriesEntry#getTimeStamp()
         */
        TIMESTAMP("timestamp", "UTCSTAMP") {
            @Override
            void appendAsDeliverableSelectValue(Formula.Mode mode, ServerExpressionNode expressionNode, Optional<IntervalLength> expertIntervalLength, VirtualReadingType targetReadingType, SqlBuilder sqlBuilder) {
                String value = expressionNode.accept(new TimeStampFromExpressionNode());
                if (value == null) {
                    sqlBuilder.append("0");
                } else if (expertIntervalLength.isPresent()) {
                    sqlBuilder.append(AggregationFunction.MAX.sqlName());
                    sqlBuilder.append("(");
                    sqlBuilder.append(value);
                    sqlBuilder.append(")");
                } else {
                    sqlBuilder.append(value);
                }
            }

            @Override
            AggregationFunction aggregationFunctionFor(VirtualReadingType readingType) {
                return AggregationFunction.MAX;
            }

            @Override
            String aggregatedValue(VirtualReadingType sourceReadingType, VirtualReadingType readingType) {
                return this.fieldSpecName();
            }
        },

        /**
         * The version of a TimeSeries interval.
         * @see com.elster.jupiter.ids.TimeSeriesEntry#getVersion()
         */
        VERSIONCOUNT("versioncount", "VERSIONCOUNT") {
            @Override
            void appendAsDeliverableSelectValue(Formula.Mode mode, ServerExpressionNode expressionNode, Optional<IntervalLength> expertIntervalLength, VirtualReadingType targetReadingType, SqlBuilder sqlBuilder) {
                sqlBuilder.append(VIRTUAL_VERSION_COUNT);
            }

            @Override
            AggregationFunction aggregationFunctionFor(VirtualReadingType readingType) {
                return AggregationFunction.MIN;
            }

            @Override
            String aggregatedValue(VirtualReadingType sourceReadingType, VirtualReadingType readingType) {
                return this.fieldSpecName();
            }
        },

        /**
         * The record time of a TimeSeries interval.
         * @see com.elster.jupiter.ids.TimeSeriesEntry#getRecordDateTime()
         */
        RECORDTIME("recordtime", "RECORDTIME") {
            @Override
            void appendAsDeliverableSelectValue(Formula.Mode mode, ServerExpressionNode expressionNode, Optional<IntervalLength> expertIntervalLength, VirtualReadingType targetReadingType, SqlBuilder sqlBuilder) {
                sqlBuilder.append(VIRTUAL_RECORD_TIME);
            }

            @Override
            AggregationFunction aggregationFunctionFor(VirtualReadingType readingType) {
                return AggregationFunction.MAX;
            }

            @Override
            String aggregatedValue(VirtualReadingType sourceReadingType, VirtualReadingType readingType) {
                return this.fieldSpecName();
            }
        },

        READINGQUALITY("readingQuality", null) {
            @Override
            void appendAsDeliverableSelectValue(Formula.Mode mode, ServerExpressionNode expressionNode, Optional<IntervalLength> expertIntervalLength, VirtualReadingType targetReadingType, SqlBuilder sqlBuilder) {
                String value = expressionNode.accept(new ReadingQualityFromExpressionNode());
                if (is(value).empty()) {
                    sqlBuilder.append("0");
                } else {
                    sqlBuilder.append("GREATEST(" + value + ")");
                }
            }

            @Override
            AggregationFunction aggregationFunctionFor(VirtualReadingType readingType) {
                return AggregationFunction.MAX;
            }
        },

        SOURCECHANNELS("sourceChannels", null) {
            @Override
            void appendAsDeliverableSelectValue(Formula.Mode mode, ServerExpressionNode expressionNode, Optional<IntervalLength> expertIntervalLength, VirtualReadingType targetReadingType, SqlBuilder sqlBuilder) {
                SourceChannelSqlNamesCollector collector = new SourceChannelSqlNamesCollector();
                expressionNode.accept(collector);
                String channels = collector.getSourceChannelSqlNames()
                        .stream()
                        .collect(Collectors.joining(" || '" + SourceChannelSetFactory.SOURCE_CHANNEL_IDS_SEPARATOR + "' || "));
                sqlBuilder.append(channels);
            }

            @Override
            AggregationFunction aggregationFunctionFor(VirtualReadingType readingType) {
                return AggregationFunction.MAX;
            }
        },

        /**
         * The value of a TimeSeries interval.
         * @see com.elster.jupiter.ids.TimeSeriesEntry#getValues()
         */
        VALUE("value", RecordSpecs.VALUE) {
            @Override
            void appendAsDeliverableSelectValue(Formula.Mode mode, ServerExpressionNode expressionNode, Optional<IntervalLength> expertIntervalLength, VirtualReadingType targetReadingType, SqlBuilder sqlBuilder) {
                sqlBuilder.add(expressionNode.accept(new ExpressionNodeToSql(mode)));
            }

            @Override
            AggregationFunction aggregationFunctionFor(VirtualReadingType readingType) {
                return readingType.aggregationFunction();
            }

        },

        /**
         * The local date of a TimeSeries interval
         */
        LOCALDATE("localdate", "LOCALDATE") {
            @Override
            void appendAsDeliverableSelectValue(Formula.Mode mode, ServerExpressionNode expressionNode, Optional<IntervalLength> expertIntervalLength, VirtualReadingType targetReadingType, SqlBuilder sqlBuilder) {
                String value = expressionNode.accept(new LocalDateFromExpressionNode());
                if (value == null) {
                    sqlBuilder.append("sysdate");
                } else if (expertIntervalLength.isPresent()) {
                    sqlBuilder.append(AggregationFunction.TRUNC.sqlName());
                    sqlBuilder.append("(");
                    sqlBuilder.append(value);
                    sqlBuilder.append(", '");
                    sqlBuilder.append(expertIntervalLength.get().toOracleTruncFormatModel());
                    sqlBuilder.append("'");
                    sqlBuilder.append(")");
                } else {
                    sqlBuilder.append(value);
                }
            }

            @Override
            AggregationFunction aggregationFunctionFor(VirtualReadingType readingType) {
                return AggregationFunction.TRUNC;
            }

            @Override
            String aggregatedValue(VirtualReadingType sourceReadingType, VirtualReadingType readingType) {
                return this.sqlName() + ", '" + readingType.getIntervalLength().toOracleTruncFormatModel() + "'";
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
         * @param mode The Formula.Mode
         * @param expressionNode The ServerExpressionNode
         * @param expertIntervalLength The target IntervalLength that is applied by the expert mode
         * @param targetReadingType The target VirtualReadingType
         * @param sqlBuilder The SqlBuilder
         */
        static void appendAllDeliverableSelectValues(Formula.Mode mode, ServerExpressionNode expressionNode, Optional<IntervalLength> expertIntervalLength, VirtualReadingType targetReadingType, SqlBuilder sqlBuilder) {
            for (TimeSeriesColumnNames columnName : values()) {
                columnName.appendAsDeliverableSelectValue(mode, expressionNode, expertIntervalLength, targetReadingType, sqlBuilder);
                if (columnName != LOCALDATE) {
                    sqlBuilder.append(", ");
                }
            }
        }

        abstract void appendAsDeliverableSelectValue(Formula.Mode mode, ServerExpressionNode expressionNode, Optional<IntervalLength> expertIntervalLength, VirtualReadingType targetReadingType, SqlBuilder sqlBuilder);

        /**
         * Append the appropriate select value to (if necessary) convert values
         * from the specified {@link VirtualReadingType source reading type} to the
         * target reading type for all TimeSeriesColumnNames to the specified SqlBuilder.
         *
         * @param sourceReadingType The source ReadingType
         * @param targetReadingType The target ReadingType
         * @param sqlBuilder The SqlBuilder
         */
        static void appendAllAggregatedSelectValues(VirtualReadingType sourceReadingType, VirtualReadingType targetReadingType, SqlBuilder sqlBuilder) {
            for (TimeSeriesColumnNames columnName : values()) {
                columnName.appendAsAggregatedSelectValue(sourceReadingType, targetReadingType, sqlBuilder);
                if (columnName != LOCALDATE) {
                    sqlBuilder.append(", ");
                }
            }
        }

        void appendAsAggregatedSelectValue(VirtualReadingType sourceReadingType, VirtualReadingType targetReadingType, SqlBuilder sqlBuilder) {
            this.aggregationFunctionFor(targetReadingType)
                    .appendTo(
                            sqlBuilder,
                            Collections.singletonList(new TextFragment(this.aggregatedValue(sourceReadingType, targetReadingType))));
        }

        abstract AggregationFunction aggregationFunctionFor(VirtualReadingType readingType);

        String aggregatedValue(VirtualReadingType sourceReadingType, VirtualReadingType readingType) {
            return this.sqlName();
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