package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.properties.PropertySpec;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Extracts values from java fields for a List of {@link PropertySpec}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-11 (16:24)
 */
public class FieldValueExtractor {

    private final Object businessObject;
    private final Class domainClass;
    private CustomPropertySetValues underConstruction = CustomPropertySetValues.empty();

    public static FieldValueExtractor from(Object businessObject) {
        return new FieldValueExtractor(businessObject);
    }

    public CustomPropertySetValues andSpecs(List<PropertySpec> specs) {
        specs.forEach(this::copy);
        return this.underConstruction;
    }

    private void copy(PropertySpec propertySpec) {
        Object value = this.valueFor(this.getField(propertySpec));
        this.underConstruction.setProperty(propertySpec.getName(), value);
    }

    private Field getField(PropertySpec propertySpec) {
        return this.getField(this.domainClass, propertySpec.getName());
    }

    private Object valueFor(Field field) {
        try {
            return field.get(this.businessObject);
        }
        catch (IllegalAccessException e) {
            throw new MappingException(e);
        }
    }

    private FieldValueExtractor(Object businessObject) {
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