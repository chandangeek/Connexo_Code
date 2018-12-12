/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.ean;

import java.io.Serializable;
import java.text.ParseException;

public class EanCode implements Serializable {

    String value;

    /**
     * Creates a new instance of EANCode
     */
    public EanCode(String ean) throws ParseException {
        if (ean == null) {
            throw new ParseException("null argument", 0);
        }
        this.value = ean.trim();
        if (!isValid()) {
            throw new ParseException(this.value, value.length());
        }
    }

    public EanCode() {
    }

    public String getValue() {
        return value;
    }


    public void setValue(String value) throws ParseException {
        if (value == null) {
            throw new ParseException("null argument", 0);
        }
        this.value = value.trim();
        if (!isValid()) {
            throw new ParseException(this.value, value.length());
        }
    }


    public String toString() {
        return value;
    }

    static public int getCheckDigit(String code) {
        return Integer.parseInt(code.substring(code.length() - 1));
    }

    static public int calculateCheckDigit(String code) {
        int multiplier = 3;
        int sum = 0;
        for (int i = code.length() - 1; i >= 0; i--) {
            int digit = Integer.parseInt(code.substring(i, i + 1));
            sum += digit * multiplier;
            multiplier = (multiplier == 3) ? 1 : 3;
        }
        int next10 = (((sum - 1) / 10) + 1) * 10;
        return next10 - sum;
    }

    static public boolean isValid(String code) {
        if (code == null || code.trim().length() == 0) {
            return false;
        }
        if (!isNumeric(code)) {
            return false;
        }
        return getCheckDigit(code) == calculateCheckDigit(code.substring(0, code.length() - 1));
    }

    static boolean isNumeric(String code) {
        int length = code.length();
        for (int i = 0; i < length; i++) {
            if (!Character.isDigit(code.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean isValid() {
        return EanCode.isValid(value);
    }

    public boolean equals(Object o) {
        if (o instanceof EanCode) {
            return value.equals(((EanCode) o).value);
        }
        return false;
    }

    public int hashCode() {
        return value.hashCode();
    }

}
