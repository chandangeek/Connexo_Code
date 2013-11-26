package com.energyict.mdc.rest.impl.properties;

import com.energyict.mdc.rest.impl.CodeTableInfo;
import com.energyict.mdc.rest.impl.TimeZoneInUseInfo;

import java.util.Date;
import java.util.Map;

/**
 * Represents simple types which a property can have
 * <p/>
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 11:44
 */
public enum SimplePropertyType {
    UNKNOWN(false) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'UNKNOWN'");
        }
    },
    NUMBER(false, Number.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'NUMBER', JSON should have properly deserialized this");
        }
    },
    TEXT(false, String.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'TEXT', JSON should have properly deserialized this");
        }
    },
    BOOLEAN(false, Boolean.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'BOOLEAN', JSON should have properly deserialized this");
        }
    },
    CLOCK(false, Date.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'CLOCK', JSON should have properly deserialized this");
        }
    },
    CODETABLE(true, CodeTableInfo.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            return new CodeTableInfo(map);
        }
    },
    TIMEZONEINUSE(true, TimeZoneInUseInfo.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            return new TimeZoneInUseInfo(map);
        }
    };

    private Class[] classes;
    private boolean isReference;

    SimplePropertyType(boolean reference, Class... classes) {
        isReference = reference;
        this.classes = classes;
    }

    /**
     * Returns the Referenced object from the JSON HashMap
     *
     * @param map the Map JSON created from the PropertyValueInfo object value
     * @return the corresponding known xxxInfo object
     */
    public abstract Object getInfoObject(Map<String, Object> map);

    public boolean isReference() {
        return isReference;
    }

    public static SimplePropertyType getTypeFrom(Class valueType) {
        for (SimplePropertyType simplePropertyType : values()) {
            for (Class aClass : simplePropertyType.classes) {
                if (aClass.isAssignableFrom(valueType)) {
                    return simplePropertyType;
                }
            }
        }
        return UNKNOWN;
    }

}
