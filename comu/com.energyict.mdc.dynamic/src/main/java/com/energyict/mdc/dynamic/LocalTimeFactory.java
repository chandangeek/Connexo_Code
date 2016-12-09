package com.energyict.mdc.dynamic;

import com.elster.jupiter.properties.AbstractValueFactory;

import java.time.LocalTime;

/**
 * Provides an implementation for the {@link com.elster.jupiter.properties.ValueFactory}
 * interface for LocalTime values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:32)
 */
public class LocalTimeFactory extends AbstractValueFactory<LocalTime> {

    @Override
    public Class<LocalTime> getValueType() {
        return LocalTime.class;
    }

    @Override
    public int getJdbcType() {
        return java.sql.Types.INTEGER;
    }

    @Override
    public LocalTime valueFromDatabase (Object object) {
        if (object != null) {
            return LocalTime.ofSecondOfDay(((Number) object).intValue());
        }
        else {
            return null;
        }
    }

    @Override
    public Object valueToDatabase (LocalTime object) {
        if (object != null) {
            return object.toSecondOfDay();
        }
        else {
            return null;
        }
    }

    @Override
    public LocalTime fromStringValue(String stringValue) {
        if (stringValue == null || stringValue.isEmpty()) {
            return LocalTime.MIDNIGHT;
        }
        else {
            return LocalTime.ofSecondOfDay(Integer.parseInt(stringValue));
        }
    }

    @Override
    public String toStringValue(LocalTime object) {
        if (object == null) {
            return "";
        }
        else {
            return Integer.toString(object.toSecondOfDay());
        }
    }

}