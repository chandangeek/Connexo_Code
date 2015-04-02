package com.elster.jupiter.properties;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;

import com.elster.jupiter.time.AllRelativePeriod;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import org.osgi.service.component.annotations.Component;

/**
 * Provides an implementation for the {@link ValueFactory} interface
 * for TimeZone values.
 *
 * relative period id == 0 => All period
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-31 (14:04)
 */
@Component(name = "com.elster.jupiter.properties.RelativePeriodFactory", service = {ValueFactory.class}, immediate = true)
public class RelativePeriodFactory extends AbstractValueFactory<RelativePeriod> {

    private TimeService timeService;

    public RelativePeriodFactory(TimeService timeService) {
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
        long id = ((Number) object).longValue();
        return getRelativePeriod(id);
    }

    @Override
    public Object valueToDatabase (RelativePeriod relativePeriod) {
        return new BigDecimal(relativePeriod.getId());
    }

    @Override
    public RelativePeriod fromStringValue (String stringValue) {
        long id = Long.parseLong(stringValue);
        return getRelativePeriod(id);
    }

    @Override
    public String toStringValue (RelativePeriod relativePeriod) {
        return "" + relativePeriod.getId();
    }

    private RelativePeriod getRelativePeriod(long id) {
        if (id == 0) {
            return new AllRelativePeriod();
        } else {
            return timeService.findRelativePeriod(id).orElse(null);
        }
    }

}