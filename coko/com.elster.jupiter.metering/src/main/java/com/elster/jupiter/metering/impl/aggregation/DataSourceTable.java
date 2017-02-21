/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

/**
 * Models the table that acts as the source from which data is aggregated.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-08-31 (14:59)
 */
interface DataSourceTable {
    String getName();
    String propertiesJoinClause(String tableName);
    String timeSeriesJoinClause(String tableName);
}