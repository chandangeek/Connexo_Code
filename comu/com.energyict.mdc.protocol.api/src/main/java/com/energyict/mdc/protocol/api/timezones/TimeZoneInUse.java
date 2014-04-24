package com.energyict.mdc.protocol.api.timezones;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.NamedBusinessObject;
import com.energyict.mdc.common.SqlBuilder;

import java.sql.SQLException;
import java.util.TimeZone;

public interface TimeZoneInUse extends NamedBusinessObject {

    /**
     * Getter for property isDefault.
     *
     * @return Value of property isDefault.
     */
    public boolean getIsDefault();

    /**
     * Sets the receiver as the default one
     *
     * @throws SQLException      if a database error occurred
     * @throws BusinessException if a business exception occurred
     */
    public void setAsDefaultTimeZone() throws SQLException, BusinessException;

    /**
     * Sets the receiver's property isDefault
     *
     * @param isDefault the new value of the property isDefault
     * @throws SQLException      if a database error occurred
     * @throws BusinessException if a business exception occurred     *
     */
    public void setIsDefault(boolean isDefault) throws SQLException, BusinessException;

    /**
     * Returns the TimeZone corresponding with the receiver
     *
     * @return the TimeZone corresponding with the receiver
     */
    public TimeZone getTimeZone();

    public void appendUtc2DateSql(SqlBuilder builder, String field);

    public void appendUtc2DateSql(SqlBuilder builder, String field, Interval period);

    public void appendUtc2DateSql(SqlBuilder sqlBuilder, String utcField, int bindVar);

    public void appendUtc2DateSql(SqlBuilder sqlBuilder, String utcField, int bindVar, Interval period);

    public void appendUtc2DateDstSql(SqlBuilder sqlBuilder, String utcField, Interval period);

    public void appendUtc2DateDstSql(SqlBuilder sqlBuilder, String utcField, int bindVar, Interval period);

}
