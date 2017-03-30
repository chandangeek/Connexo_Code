/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl;

import com.elster.jupiter.demo.impl.builders.Builder;
import com.elster.jupiter.demo.impl.builders.NamedBuilder;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

public final class Log {
    private static final boolean IS_PRODUCTION = false;

    public static <T extends Builder<?>> void write(T factory) {
        if (!IS_PRODUCTION && factory != null) {
            StringBuilder log = new StringBuilder();
            boolean hasParameters = false;
            try {
                if (factory instanceof NamedBuilder) {
                    hasParameters = writeParameters(NamedBuilder.class, factory, log);
                }
                hasParameters = hasParameters | writeParameters(factory.getClass(), factory, log);
                if (hasParameters) {
                    log.insert(0, ". Parameters: ");
                }
                log.insert(0, whatCreate(factory));
                log.insert(0, " ==> Creating a ");
            } catch (ReflectiveOperationException e) {
                // ignore it, this output is only for dev team
            }
            System.out.println(log.toString());
        }
    }

    private static <T> boolean writeParameters(Class<?> explicitClass, T factory, StringBuilder log) throws IllegalAccessException {
        Field[] factoryFields = explicitClass.getDeclaredFields();
        boolean hasParameters = false;

        for (Field field : factoryFields) {
            if (!Modifier.isFinal(field.getModifiers())) {
                hasParameters = true;
                field.setAccessible(true);
                log.append("\n\t").append(field.getName()).append(" = ").append(objToReadableString(field.get(factory)));
            }
        }
        return hasParameters;
    }

    private static <T extends Builder<?>> String whatCreate(T factory) {
        try {
            Method getMethod = factory.getClass().getDeclaredMethod("create");
            return getMethod.getReturnType().getSimpleName();
        } catch (NoSuchMethodException e) {
            // ignore it, this output is only for dev team
        }
        return " [unable to determine] ";
    }

    private static <T> String objToReadableString(T obj) {
        if (obj != null) {
            String out = obj.toString();
            if (out.contains("@")) { // standard serialization
                StringBuilder readableOutput = new StringBuilder();
                readableOutput.append(" [");
                if (obj instanceof String[]) {
                    Arrays.stream((String[]) obj).forEach(s -> readableOutput.append(s).append(", "));
                    readableOutput.setLength(readableOutput.length() - 2);
                } else {
                    readableOutput.append(obj.getClass().getSimpleName());
                }
                if (obj instanceof ArrayList) {
                    ((ArrayList<?>) obj).forEach(o -> readableOutput.append(objToReadableString(o)).append(", "));
                    readableOutput.setLength(readableOutput.length() - 2);
                }
                if (obj instanceof HasId) {
                    readableOutput.append(" id = ").append(((HasId) obj).getId());
                }
                if (obj instanceof HasName) {
                    readableOutput.append(" name = ").append(((HasName) obj).getName());
                }
                readableOutput.append("]");
                out = readableOutput.toString();
            }
            return out;
        }
        return "null";
    }
}
