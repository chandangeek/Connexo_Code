package com.elster.jupiter.users;

public enum FormatKey {
    //date
    SHORT_DATE("format.date.short"),
    LONG_DATE("format.date.long"),
    
    //time
    SHORT_TIME("format.time.short"),
    LONG_TIME("format.time.long"),
    
    //date-time
    SHORT_DATETIME("format.datetime.short"),
    LONG_DATETIME("format.datetime.long"),
    
    //separators for numbers
    DECIMAL_SEPARATOR("format.number.decimalseparator"),
    THOUSANDS_SEPARATOR("format.number.thousandsseparator"),
    
    //number precision
    DECIMAL_PRECISION("format.number.decimalprecision"),
    
    //currency
    CURRENCY("format.number.currency")
    ;

    private String key;
    
    private FormatKey(String key) {
        this.key = key;
    }
    
    public String getKey() {
        return key;
    }
}
