/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;

import java.math.BigDecimal;

/**
 * Provides an implementation for the {@link ValueFactory} interface
 * for TimeZone values.
 *
 * relative period id == 0 => All period
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-31 (14:04)
 */
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
    public int getJdbcType () {
        return java.sql.Types.NUMERIC;
    }

    @Override
    public RelativePeriod valueFromDatabase (Object object) {
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
            return timeService.getAllRelativePeriod();
        } else {
            return timeService.findRelativePeriod(id).orElse(null);
        }
    }

}