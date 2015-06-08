package com.elster.jupiter.rest.util;

/**
 * This class described a property's meta data: name
 * Created by bvn on 6/8/15.
 */
public class PropertyDescriptionInfo {
    public String propertyName;
    public String type;
    public String displayValue;

    public PropertyDescriptionInfo() {
    }

    public PropertyDescriptionInfo(String propertyName, Class<?> aClass, String displayValue) {
        this.propertyName = propertyName;
        this.type = aClass.getSimpleName();
        this.displayValue = displayValue;
    }

}
