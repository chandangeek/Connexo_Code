/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

public enum ValidationAction {
    WARN_ONLY("warnOnly"), FAIL("fail");  // TODO check with Karel for completeness

    private final String value;

    private ValidationAction(String value) {
        this.value = value;
    }

    public static ValidationAction get(int id) {
        return values()[id - 1];
    }

    public int getId() {
        return ordinal() + 1;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

}