/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict;

import com.elster.jupiter.cps.HardCodedFieldNames;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.fail;

/**
 * Serves as the root for test classes that will
 * check the implementation of persistent entity
 * classes that hold custom properties of connection types,
 * security properties or protocol dialects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-04 (14:48)
 */
public abstract class CustomPropertiesPersistenceTest {

    public static final int MAX_COLUMN_NAME_LENGTH = 30;

    protected <T extends PersistentDomainExtension<?>> void checkJavaxAnnotationsOnFields(Class<T> clazz) {
        this.getAllFields(clazz)
            .filter(this::isNotPrimaryKeyField)
            .filter(this::isNotStatic)
            .forEach(this::checkJavaxAnnotationsOnField);
    }

    private boolean isNotPrimaryKeyField(Field field) {
        return !CommonDeviceProtocolDialectProperties.Fields.DIALECT_PROPERTY_PROVIDER.javaName().equals(field.getName())
            && !CommonBaseDeviceSecurityProperties.Fields.DEVICE.javaName().equals(field.getName())
            && !CommonBaseDeviceSecurityProperties.Fields.PROPERTY_SPEC_PROVIDER.javaName().equals(field.getName())
            && !HardCodedFieldNames.CUSTOM_PROPERTY_SET.javaName().equals(field.getName())
            && !HardCodedFieldNames.INTERVAL.javaName().equals(field.getName());
    }

    private boolean isNotStatic(Field field) {
        return !Modifier.isStatic(field.getModifiers());
    }

    private void checkJavaxAnnotationsOnField(Field field) {
        notAllowedAnnotationClasses().forEach(annotationClass -> this.checkNotAnnotated(field, annotationClass));
        if (String.class.equals(field.getType())) {
            this.checkJavaxAnnotationsOnStringField(field);
        }
    }

    private Stream<Class> notAllowedAnnotationClasses() {
        return Stream.of(
                NotEmpty.class,
                NotNull.class,
                NotBlank.class,
                com.elster.jupiter.domain.util.NotEmpty.class);
    }

    private void checkNotAnnotated(Field field, Class annotationClass) {
        if (field.getAnnotation(annotationClass) != null) {
            fail("Field " + field.getName() + " of class " + field.getDeclaringClass().getName() + " should not be annotated with " + annotationClass.getName() + " even if the property is required because it is possible that the actual value will be stored at the configuration level");
        }
    }

    private void checkJavaxAnnotationsOnStringField(Field field) {
        if (field.getAnnotation(Size.class) == null) {
            fail("String Field " + field.getName() + " of class " + field.getDeclaringClass().getName() + " should have been annotated with " + Size.class.getName() + " to allow the ORM framework to determine the number of characters to use for the VARCHAR datatype");
        }
    }

    protected boolean fieldDoesNotExists(Class clazz, String fieldName) {
        return !this.getField(clazz, fieldName).isPresent();
    }

    private Optional<Field> getField(Class<?> clazz, String fieldName) {
        return this.getAllFields(clazz)
                .filter(field -> field.getName().equals(fieldName))
                .findAny();
    }

    private Stream<Field> getAllFields(Class<?> clazz) {
        return this.getClassesInHierarchy(clazz)
                .flatMap(cl -> Stream.of(cl.getDeclaredFields()));
    }

    private Stream<Class> getClassesInHierarchy(Class<?> rootClass) {
        List<Class> classHierarchy = new ArrayList<>();
        Class<?> current = rootClass;
        do {
            classHierarchy.add(current);
            current = current.getSuperclass();
        } while (current != null);
        return classHierarchy.stream();
    }

}