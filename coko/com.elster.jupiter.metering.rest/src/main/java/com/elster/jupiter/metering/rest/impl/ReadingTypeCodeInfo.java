/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

public class ReadingTypeCodeInfo {
    public int code;
    public String displayName;

    public ReadingTypeCodeInfo(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public ReadingTypeCodeInfo(int code) {
        this.code = code;
        this.displayName = String.valueOf(code);
    }
}
