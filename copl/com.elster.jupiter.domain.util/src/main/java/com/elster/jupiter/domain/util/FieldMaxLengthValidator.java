/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.domain.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

public class FieldMaxLengthValidator {
    private static final int MAX_LENGTH = 4000;

    private FieldMaxLengthValidator(){}

    public static void validate(Object object) throws FieldMaxLengthException {
        Field[] allFields = object.getClass().getDeclaredFields();
        for (Field field : allFields) {
            try {
                field.setAccessible(true);
                Object obj = field.get(object);
                if (obj != null) {
                    if (obj instanceof String || obj instanceof BigInteger || obj instanceof BigDecimal) {
                        if (String.valueOf(obj).length() > MAX_LENGTH) {
                            throw new FieldMaxLengthException(field.getName() + " is too long, max length must be less than 4000");
                        }
                    }
                }
            } catch (IllegalAccessException ignored) {
                // not reachable case
            }
        }
    }
}
