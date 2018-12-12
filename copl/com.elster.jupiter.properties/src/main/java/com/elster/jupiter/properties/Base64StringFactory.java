/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.properties;

import java.util.Base64;

/**
 * A Base64 string is really just a String, this class only provides means to validate the content
 */
public class Base64StringFactory extends StringFactory {
    @Override
    public boolean isValid(String value) {
        try {
            Base64.getDecoder().decode(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
