package com.elster.jupiter.rest.util;

/**
 * This class described a property's meta data: name
 * Created by bvn on 6/8/15.
 */
public class PropertyDescriptionInfo {
    public String name;
    public Class<?> type;
    public String displayValue;

    public PropertyDescriptionInfo(String name, Class<?> type, String displayValue) {
        this.name = name;
        this.type = type;
        this.displayValue = displayValue;
    }
}
