package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.HardCodedFieldNames;
import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.util.time.Interval;

import java.lang.reflect.Field;

/**
 * Provides access to Interval information contained in business objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-11 (16:24)
 */
public class IntervalAccessor {

    /**
     * Gets an {@link Interval} value from the specified business object.
     *
     * @param businessObject The business object
     * @return The value of the Interval field
     * @see HardCodedFieldNames#INTERVAL
     */
    public static Interval getValue(Object businessObject) {
        return getValue(getField(businessObject.getClass(), HardCodedFieldNames.INTERVAL.javaName()), businessObject);
    }

    /**
     * Sets an {@link Interval} value into the specified business object.
     *
     * @param businessObject The business object
     * @param interval The Interval
     * @see HardCodedFieldNames#INTERVAL
     */
    public static void setValue(Object businessObject, Interval interval) {
        setValue(getField(businessObject.getClass(), HardCodedFieldNames.INTERVAL.javaName()), businessObject, interval);
    }

    private static Interval getValue(Field field, Object businessObject) {
        try {
            return (Interval) field.get(businessObject);
        }
        catch (IllegalAccessException e) {
            throw new MappingException(e);
        }
    }

    private static void setValue(Field field, Object businessObject, Interval value) {
        try {
            field.set(businessObject, value);
        }
        catch (IllegalAccessException e) {
            throw new MappingException(e);
        }
    }

    private static Field getField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        do {
            for (Field field : current.getDeclaredFields()) {
                if (field.getName().equals(fieldName)) {
                    field.setAccessible(true);
                    return field;
                }
            }
            current = current.getSuperclass();
        } while (current != null);
        throw new MappingException(clazz, fieldName);
    }

}