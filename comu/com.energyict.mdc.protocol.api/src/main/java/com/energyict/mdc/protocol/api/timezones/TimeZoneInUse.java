/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.timezones;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.SqlBuilder;

import java.sql.SQLException;
import java.util.TimeZone;

public interface TimeZoneInUse {

    String getName();

    int getId();

    /**
     * Getter for property isDefault.
     *
     * @return Value of property isDefault.
     */
    boolean getIsDefault();

    /**
     * Sets the receiver as the default one
     *
     * @throws SQLException      if a database error occurred
     */
    void setAsDefaultTimeZone() throws SQLException;

    /**
     * Sets the receiver's property isDefault
     *
     * @param isDefault the new value of the property isDefault
     * @throws SQLException      if a database error occurred
     */
    void setIsDefault(boolean isDefault) throws SQLException;

    /**
     * Returns the TimeZone corresponding with the receiver
     *
     * @return the TimeZone corresponding with the receiver
     */
    TimeZone getTimeZone();

    void appendUtc2DateSql(SqlBuilder builder, String field);

    void appendUtc2DateSql(SqlBuilder builder, String field, Interval period);

    void appendUtc2DateSql(SqlBuilder sqlBuilder, String utcField, int bindVar);

    void appendUtc2DateSql(SqlBuilder sqlBuilder, String utcField, int bindVar, Interval period);

    void appendUtc2DateDstSql(SqlBuilder sqlBuilder, String utcField, Interval period);

    void appendUtc2DateDstSql(SqlBuilder sqlBuilder, String utcField, int bindVar, Interval period);

}
