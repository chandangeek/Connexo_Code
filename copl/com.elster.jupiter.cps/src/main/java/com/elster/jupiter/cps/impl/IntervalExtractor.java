package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.HardCodedFieldNames;
import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.properties.PropertySpec;

import java.lang.reflect.Field;

/**
 * Extracts values from java fields for a List of {@link PropertySpec}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-11 (16:24)
 */
public class IntervalExtractor {

    private final Object businessObject;
    private final Class domainClass;

    public static IntervalExtractor from(Object businessObject) {
        return new IntervalExtractor(businessObject);
    }

    public void into(CustomPropertySetValues properties) {
        this.copy(HardCodedFieldNames.INTERVAL.javaName(), properties);
    }

    private void copy(String fieldName, CustomPropertySetValues properties) {
        Object value = this.valueFor(this.getField(fieldName));
        properties.setProperty(fieldName, value);
    }

    private Field getField(String fieldName) {
        return this.getField(this.domainClass, fieldName);
    }

    private Object valueFor(Field field) {
        try {
            return field.get(this.businessObject);
        }
        catch (IllegalAccessException e) {
            throw new MappingException(e);
        }
    }

    private IntervalExtractor(Object businessObject) {
        super();
        this.businessObject = businessObject;
        this.domainClass = businessObject.getClass();
    }

    private Field getField(Class<?> clazz, String fieldName) {
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