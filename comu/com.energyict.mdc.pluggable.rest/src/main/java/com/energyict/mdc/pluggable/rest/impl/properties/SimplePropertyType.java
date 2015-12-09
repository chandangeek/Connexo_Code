package com.energyict.mdc.pluggable.rest.impl.properties;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ThreeStateFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.rest.util.properties.PropertyType;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.DateFactory;
import com.energyict.mdc.dynamic.Ean13Factory;
import com.energyict.mdc.dynamic.Ean18Factory;
import com.energyict.mdc.dynamic.EncryptedStringFactory;
import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.LargeStringFactory;
import com.energyict.mdc.dynamic.ObisCodeValueFactory;
import com.energyict.mdc.dynamic.PasswordFactory;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.dynamic.TimeOfDayFactory;
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
    CODETABLE(Code.class, true),
    TIMEZONEINUSE(TimeZoneInUse.class, true),
    USERFILEREFERENCE(UserFile.class, true),
    EAN13(Ean13Factory.class),
    EAN18(Ean18Factory.class),
    DATE(DateFactory.class),
    TEXTAREA(LargeStringFactory.class),
    ENCRYPTED_STRING(EncryptedStringFactory.class),
    OBISCODE(ObisCodeValueFactory.class),
    READINGTYPE(ReadingType.class),
    LOADPROFILETYPE(LoadProfileType.class, true),
    LOADPROFILE(LoadProfile.class, true),
    LOGBOOK(LogBook.class, true),
    REGISTER(Register.class, true),
    TEXT(StringFactory.class),
    FIRMWAREVERSION(FirmwareVersion.class, true),
    TIMESTAMP(InstantFactory.class),
    ;

    private Class domainClass;
    private boolean isReference;

    SimplePropertyType(Class valueFactoryClass) {
        this(valueFactoryClass, false);
    }

    SimplePropertyType(Class domainClass, boolean reference) {
        this.isReference = reference;
        this.domainClass = domainClass;
    }

    public boolean isReference() {
        return isReference;
    }

    private boolean matches (ValueFactory valueFactory) {
        return this.domainClass.isAssignableFrom(valueFactory.getValueType());
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
