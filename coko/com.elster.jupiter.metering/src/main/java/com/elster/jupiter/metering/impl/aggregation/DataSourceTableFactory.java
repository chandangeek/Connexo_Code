/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

/**
 * Provides implementations for the {@link DataSourceTable} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-01 (08:38)
 */
final class DataSourceTableFactory {

    private static final String STARTTIMESTART_TIME = "starttime";
    private static final String END_TIME = "endtime";

    static DataSourceTable dual() {
        return new Dual();
    }

    static DataSourceTable timeSeries(String tableName) {
        return new TimeSeriesTable(tableName);
    }

    static DataSourceTable customProperties(String tableName) {
        return new CustomPropertyTable(tableName);
    }

    // Hide constructor for factory class with static methods only
    private DataSourceTableFactory() {}

    private static String timestampFrom(String tableName) {
        return fullyQualified(tableName, SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName());
    }

    private static String fullyQualified(String tableName, String columnName) {
        return tableName + "." + columnName;
    }

    private static class TimeSeriesTable implements DataSourceTable {
        private final String name;

        private TimeSeriesTable(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String propertiesJoinClause(String tableName) {
            return " JOIN " + tableName + " ON (    " + fullyQualified(tableName, STARTTIMESTART_TIME) + " < " + timestampFrom(this.name) +
                    "                            AND " + timestampFrom(this.name) + " <= " + fullyQualified(tableName, END_TIME) + ")";
        }

        @Override
        public String timeSeriesJoinClause(String tableName) {
            return " JOIN " + tableName + " ON " + timestampFrom(tableName) + " = " + timestampFrom(this.name);
        }
    }

    private static class CustomPropertyTable implements DataSourceTable {
        private final String name;

        private CustomPropertyTable(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String timeSeriesJoinClause(String tableName) {
            return " JOIN " + tableName + " ON (    " + fullyQualified(this.name, STARTTIMESTART_TIME) + " < " + timestampFrom(tableName) +
                    "                            AND " + timestampFrom(tableName) + " <= " + fullyQualified(this.name, END_TIME) + ")";
        }

        @Override
        public String propertiesJoinClause(String tableName) {
            return " JOIN " + tableName + " ON (   (    " + fullyQualified(this.name, STARTTIMESTART_TIME) + " <= " + fullyQualified(tableName, STARTTIMESTART_TIME) +
                    "                                AND " + fullyQualified(this.name, END_TIME) + " >= " + fullyQualified(tableName, STARTTIMESTART_TIME) + ")" +
                    "                            OR (    " + fullyQualified(tableName, STARTTIMESTART_TIME) + " <= " + fullyQualified(this.name, STARTTIMESTART_TIME) +
                    "                                AND " + fullyQualified(tableName, END_TIME) + " >= " + fullyQualified(this.name, STARTTIMESTART_TIME) + "))";
        }
    }

    private static class Dual implements DataSourceTable {
        @Override
        public String getName() {
            return "dual";
        }

        @Override
        public String propertiesJoinClause(String tableName) {
            return this.joinClause(tableName);
        }

        @Override
        public String timeSeriesJoinClause(String tableName) {
            return this.joinClause(tableName);
        }

        private String joinClause(String tableName) {
            return " JOIN " + tableName;
        }
    }

}