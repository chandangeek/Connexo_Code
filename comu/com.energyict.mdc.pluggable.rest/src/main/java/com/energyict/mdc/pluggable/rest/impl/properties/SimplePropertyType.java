package com.energyict.mdc.pluggable.rest.impl.properties;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ThreeStateFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.rest.util.properties.PropertyType;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.dynamic.*;
import com.energyict.mdc.firmware.FirmwareVersion;
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
public enum SimplePropertyType implements PropertyType {
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
    EAN13(Ean13Factory.class),
    EAN18(Ean18Factory.class),
    DATE(DateFactory.class),
    TEXTAREA(LargeStringFactory.class),
    ENCRYPTED_STRING(EncryptedStringFactory.class),
    OBISCODE(ObisCodeValueFactory.class),
    READINGTYPE(ReadingType.class),
    LOADPROFILETYPE(JupiterReferenceFactory.class, LoadProfileType.class),
    LOADPROFILE(JupiterReferenceFactory.class, LoadProfile.class),
    LOGBOOK(JupiterReferenceFactory.class, LogBook.class),
    REGISTER(JupiterReferenceFactory.class, Register.class),
    TEXT(StringFactory.class),
    FIRMWAREVERSION(JupiterReferenceFactory.class, FirmwareVersion.class),
    TIMESTAMP(InstantFactory.class),
    ;

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

    SimplePropertyType(Class valueFactoryClass, Class domainClass) {
        this.isReference = true;
        this.valueFactoryClass = valueFactoryClass;
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
