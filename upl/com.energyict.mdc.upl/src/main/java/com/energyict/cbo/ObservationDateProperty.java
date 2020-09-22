package com.energyict.cbo;

import java.math.BigDecimal;
import java.util.Date;

public class ObservationDateProperty extends ObservationProperty<Date> {

    public ObservationDateProperty(String name, Date value) {
        super(name, value);
    }

    public ObservationDateProperty(String name, Object value) {
        super(name, parse(value));
    }

    private static Date parse(Object lastSeenDateObject) {
        if (lastSeenDateObject instanceof Integer) {
            return new Date(Long.valueOf((Integer) lastSeenDateObject));
        } else if (lastSeenDateObject instanceof Long) {
            return new Date((Long) lastSeenDateObject);
        } else if (lastSeenDateObject instanceof BigDecimal) {
            return new Date(((BigDecimal) lastSeenDateObject).longValue());
        } else if (lastSeenDateObject instanceof Date) {
            return (Date) lastSeenDateObject;   //Epoch
        } else if (lastSeenDateObject instanceof String) {
            try {
                return new Date(Long.valueOf((String) lastSeenDateObject));
            } catch (NumberFormatException e) {
                // this seems wrong to be: silent failure! but this is a refactoring and fixing this should assume throwing a checked exception... TODO
                return null;        //Non numeric value is not supported.
            }
        }
        return null;
    }

}
