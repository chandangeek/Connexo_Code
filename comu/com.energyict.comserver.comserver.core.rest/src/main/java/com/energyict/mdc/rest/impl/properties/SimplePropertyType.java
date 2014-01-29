package com.energyict.mdc.rest.impl.properties;

import com.energyict.mdc.dynamic.*;
import com.energyict.mdc.metering.mapping.ObisCodeToReadingTypeFactory;
import com.energyict.mdc.rest.impl.CodeTableInfo;
import com.energyict.mdc.rest.impl.LoadProfileTypeInfo;
import com.energyict.mdc.rest.impl.TimeZoneInUseInfo;
import com.energyict.mdc.rest.impl.UserFileReferenceInfo;
import com.energyict.mdw.core.CodeFactory;
import com.energyict.mdw.core.LoadProfileTypeFactory;
import com.energyict.mdw.core.TimeZoneInUseFactory;
import com.energyict.mdw.core.UserFileFactory;

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
    PASSWORD(false, PasswordFactory.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'PASSWORD', JSON should have properly deserialized this");
        }
    },
    HEXSTRING(false, HexStringFactory.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'HEXSTRING', JSON should have properly deserialized this");
        }
    },
    NUMBER(false, BigDecimalFactory.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'NUMBER', JSON should have properly deserialized this");
        }
    },
    NULLABLE_BOOLEAN(false, ThreeStateFactory.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'NULLABLE_BOOLEAN', JSON should have properly deserialized this");
        }
    },
    BOOLEAN(false, BooleanFactory.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'BOOLEAN', JSON should have properly deserialized this");
        }
    },
    TIMEDURATION(false, TimeDurationValueFactory.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'TIMEDURATION', JSON should have properly deserialized this");
        }
    },
    TIMEOFDAY(false, TimeOfDayFactory.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'TIMEOFDAY', JSON should have properly deserialized this");
        }
    },
    CLOCK(false, DateAndTimeFactory.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'CLOCK', JSON should have properly deserialized this");
        }
    },
    CODETABLE(true, CodeFactory.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            return new CodeTableInfo(map);
        }
    },
    TIMEZONEINUSE(true, TimeZoneInUseFactory.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            return new TimeZoneInUseInfo(map);
        }
    },
    USERFILEREFERENCE(true, UserFileFactory.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            return new UserFileReferenceInfo(map);
        }
    },
    LOADPROFILETYPE(true, LoadProfileTypeFactory.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            return new LoadProfileTypeInfo(map);
        }
    },
  /*  LOADPROFILE(true, LoadProfileFactory.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            return new LoadProfileInfo(map);
        }
    },*/
    EAN13(false, Ean13Factory.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'EAN13', JSON should have properly deserialized this");
        }
    },
    EAN18(false, Ean18Factory.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'EAN13', JSON should have properly deserialized this");
        }
    },
    SPATIAL_COORDINATES(false, SpatialCoordinatesFactory.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'EAN13', JSON should have properly deserialized this");
        }
    },
    DATE(false, DateFactory.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'DATE', JSON should have properly deserialized this");
        }
    },

    TEXTAREA(false, LargeStringFactory.class) {
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'TEXTAREA', JSON should have properly deserialized this");
        }
    },

    ENCRYPTED_STRING(false, EncryptedStringFactory.class) {
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'ENCRYPTED_STRING', JSON should have properly deserialized this");
        }
    },

    OBISCODE(false, ObisCodeValueFactory.class) {
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'OBISCODE', JSON should have properly deserialized this");
        }
    },

    READINGTYPE(false, ObisCodeToReadingTypeFactory.class) {
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'READINTYPE', JSON should have properly deserialized this");
        }
    },

    TEXT(false, StringFactory.class) {
        @Override
        public Object getInfoObject(Map<String, Object> map) {
            throw new UnsupportedOperationException("GetInfoObject is not supported on the type 'TEXT', JSON should have properly deserialized this");
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

    public static SimplePropertyType getTypeFrom(ValueFactory valueFactory) {
        for (SimplePropertyType simplePropertyType : values()) {
            for (Class aClass : simplePropertyType.classes) {
                if (aClass.isAssignableFrom(valueFactory.getClass())) {
                    return simplePropertyType;
                }
            }
        }
        return UNKNOWN;
    }

}
