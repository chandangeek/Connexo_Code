/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

public class IdWithDisplayValueInfo<T> {
    public T id;
    public String displayValue;

    public IdWithDisplayValueInfo() {
    }

    public IdWithDisplayValueInfo(T id, String displayValue) {
        this.id = id;
        this.displayValue = displayValue;
    }
}
