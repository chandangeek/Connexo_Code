package com.energyict.mdc.rest.impl.properties;

import java.util.Date;

/**
 * Represents simple types which a property can have
 *
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 11:44
 */
public enum SimplePropertyType {
    NUMBER(Number.class),
    STRING(String.class),
//    LARGE_STRING, // more then one line of text
    BOOLEAN(Boolean.class),
    CLOCK(Date.class),
    REFERENCE;

    private Class[] classes;

    SimplePropertyType(Class... classes) {
        this.classes = classes;
    }

    public static SimplePropertyType getTypeFrom(Class valueType) {
        for (SimplePropertyType simplePropertyType : values()) {
            for (Class aClass : simplePropertyType.classes) {
                if(aClass.isAssignableFrom(valueType)){
                    return simplePropertyType;
                }
            }
        }
        return REFERENCE;
    }


}
