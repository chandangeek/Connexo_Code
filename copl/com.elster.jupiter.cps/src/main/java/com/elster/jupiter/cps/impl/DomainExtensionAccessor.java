package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.HardCodedFieldNames;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.util.time.Interval;

import java.lang.reflect.Field;

/**
 * Provides access to the Interval and {@link RegisteredCustomPropertySet}
 * fields of {@link PersistentDomainExtension}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-11 (16:24)
 */
public class DomainExtensionAccessor {

    /**
     * Gets the {@link Interval} from the specified {@link PersistentDomainExtension}.
     *
     * @param domainExtension The PersistentDomainExtension
     * @return The value of the Interval field
     * @see HardCodedFieldNames#INTERVAL
     */
    public static Interval getInterval(PersistentDomainExtension domainExtension) {
        return (Interval) getValue(getField(domainExtension.getClass(), HardCodedFieldNames.INTERVAL.javaName()), domainExtension);
    }

    /**
     * Sets the {@link Interval} into the specified {@link PersistentDomainExtension}.
     *
     * @param domainExtension The PersistentDomainExtension
     * @param interval The Interval
     * @see HardCodedFieldNames#INTERVAL
     */
    public static void setInterval(PersistentDomainExtension domainExtension, Interval interval) {
        setValue(getField(domainExtension.getClass(), HardCodedFieldNames.INTERVAL.javaName()), domainExtension, interval);
    }

    /**
     * Sets the {@link RegisteredCustomPropertySet} into the specified {@link PersistentDomainExtension}.
     *
     * @param domainExtension The PersistentDomainExtension
     * @param registeredCustomPropertySet The RegisteredCustomPropertySet
     * @see HardCodedFieldNames#CUSTOM_PROPERTY_SET
     */
    public static void setRegisteredCustomPropertySet(PersistentDomainExtension domainExtension, RegisteredCustomPropertySet registeredCustomPropertySet) {
        setReferenceValue(getField(domainExtension.getClass(), HardCodedFieldNames.CUSTOM_PROPERTY_SET.javaName()), domainExtension, registeredCustomPropertySet);
    }

    private static Object getValue(Field field, PersistentDomainExtension domainExtension) {
        try {
            return field.get(domainExtension);
        }
        catch (IllegalAccessException e) {
            throw new MappingException(e);
        }
    }

    private static void setValue(Field field, PersistentDomainExtension domainExtension, Object value) {
        try {
            field.set(domainExtension, value);
        }
        catch (IllegalAccessException e) {
            throw new MappingException(e);
        }
    }

    private static void setReferenceValue(Field field, PersistentDomainExtension domainExtension, Object value) {
        try {
            Reference reference = (Reference) field.get(domainExtension);
            reference.set(value);
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