package com.energyict.mdc.pluggable.rest.impl.properties;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.dynamic.BigDecimalFactory;
import com.energyict.mdc.dynamic.BooleanFactory;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.DateFactory;
import com.energyict.mdc.dynamic.Ean13Factory;
import com.energyict.mdc.dynamic.Ean18Factory;
import com.energyict.mdc.dynamic.EncryptedStringFactory;
import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.LargeStringFactory;
import com.energyict.mdc.dynamic.ObisCodeValueFactory;
import com.energyict.mdc.dynamic.PasswordFactory;
import com.energyict.mdc.dynamic.SpatialCoordinatesFactory;
import com.energyict.mdc.dynamic.StringFactory;
import com.energyict.mdc.dynamic.ThreeStateFactory;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.dynamic.TimeOfDayFactory;
import com.energyict.mdc.dynamic.ValueFactory;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.api.timezones.TimeZoneInUse;

/**
 * Represents simple types which a property can have
 * <p/>
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 11:44
 */
public enum SimplePropertyType {
    UNKNOWN(Void.class),
    PASSWORD(PasswordFactory.class),
    HEXSTRING(HexStringFactory.class),
    NUMBER(BigDecimalFactory.class),
    NULLABLE_BOOLEAN(ThreeStateFactory.class),
    BOOLEAN(BooleanFactory.class),
    TIMEDURATION(TimeDurationValueFactory.class),
    TIMEOFDAY(TimeOfDayFactory.class),
    CLOCK(DateAndTimeFactory.class),
    CODETABLE(FactoryIds.CODE, Code.class),
    TIMEZONEINUSE(FactoryIds.TIMEZONE_IN_USE, TimeZoneInUse.class),
    USERFILEREFERENCE(FactoryIds.USERFILE, UserFile.class),
    LOADPROFILETYPE(FactoryIds.LOADPROFILE_TYPE, LoadProfileType.class),
    EAN13(Ean13Factory.class),
    EAN18(Ean18Factory.class),
    SPATIAL_COORDINATES(SpatialCoordinatesFactory.class),
    DATE(DateFactory.class),
    TEXTAREA(LargeStringFactory.class),
    ENCRYPTED_STRING(EncryptedStringFactory.class),
    OBISCODE(ObisCodeValueFactory.class),
    READINGTYPE(ReadingType.class),
    TEXT(StringFactory.class);

    private Class valueFactoryClass;
    private Class domainClass;
    private FactoryIds factoryId;
    private boolean isReference;

    SimplePropertyType(Class valueFactoryClass) {
        this.isReference = false;
        this.valueFactoryClass = valueFactoryClass;
    }

    SimplePropertyType(FactoryIds factoryId, Class domainClass) {
        this.isReference = true;
        this.factoryId = factoryId;
        this.domainClass = domainClass;
    }

    public boolean isReference() {
        return isReference;
    }

    private boolean matches (ValueFactory valueFactory) {
        if (this.isReference()) {
            return this.domainClass.isAssignableFrom(valueFactory.getValueType());
        }
        else {
            return this.valueFactoryClass.isAssignableFrom(valueFactory.getClass());
        }
    }

    public static SimplePropertyType getTypeFrom(ValueFactory valueFactory) {
        for (SimplePropertyType simplePropertyType : values()) {
            if (simplePropertyType.matches(valueFactory)) {
                return simplePropertyType;
            }
        }
        return UNKNOWN;
    }

}
