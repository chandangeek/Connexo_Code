/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users;

public enum PreferenceType {
    //date
    SHORT_DATE("format.date.short"),
    LONG_DATE("format.date.long"),
    
    //time
    SHORT_TIME("format.time.short"),
    LONG_TIME("format.time.long"),
    
    //date-time
    DATETIME_SEPARATOR("format.datetime.separator"),
    DATETIME_ORDER("format.datetime.order"),
    
    //separators for numbers
    DECIMAL_SEPARATOR("format.number.decimalseparator"),
    THOUSANDS_SEPARATOR("format.number.thousandsseparator"),
    
    //number precision
    DECIMAL_PRECISION("format.number.decimalprecision"),
    
    //currency
    CURRENCY("format.number.currency")
    ;

    private String key;
    
    private PreferenceType(String key) {
        this.key = key;
    }
    
    public String getTranslationKey() {
        return key;
    }
}
