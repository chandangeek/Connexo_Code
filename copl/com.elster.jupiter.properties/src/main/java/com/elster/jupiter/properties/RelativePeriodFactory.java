package com.elster.jupiter.properties;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;

import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;

/**
 * Provides an implementation for the {@link ValueFactory} interface
 * for TimeZone values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-31 (14:04)
 */
public class RelativePeriodFactory extends AbstractValueFactory<RelativePeriod> {

    private TimeService timeService;

    public RelativePeriodFactory(TimeService timeservice) {
        this.timeService = timeService;
    }

    @Override
    public Class<RelativePeriod> getValueType () {
        return RelativePeriod.class;
    }

    @Override
    public String getDatabaseTypeName () {
        return "number";
    }

    @Override
    public int getJdbcType () {
        return java.sql.Types.NUMERIC;
    }

    @Override
    public RelativePeriod valueFromDatabase (Object object) throws SQLException {
        return getRelativePeriod(((Number) object).longValue());
    }

    @Override
    public Object valueToDatabase (RelativePeriod relativePeriod) {
        return new BigDecimal(relativePeriod.getId());
    }

    @Override
    public RelativePeriod fromStringValue (String stringValue) {
        if (stringValue == null) {
            return null;
        }
        else {
            return getRelativePeriod(Long.parseLong(stringValue));
        }
    }

    @Override
    public String toStringValue (RelativePeriod relativePeriod) {
        return "" + relativePeriod.getId();
    }

    private RelativePeriod getRelativePeriod(long id) {
        return timeService.findRelativePeriod(id).orElse(null);
    }

}