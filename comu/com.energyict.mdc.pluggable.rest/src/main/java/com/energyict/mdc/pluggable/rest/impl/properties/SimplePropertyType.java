package com.energyict.mdc.pluggable.rest.impl.properties;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.PropertyType;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TimeOfDay;
import com.energyict.mdc.common.ean.Ean13;
import com.energyict.mdc.common.ean.Ean18;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.LargeStringFactory;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.DeviceMessageFile;
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;
import com.energyict.mdc.protocol.api.timezones.TimeZoneInUse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

/**
 * Represents simple types which a property can have
 * <p/>
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 11:44
 */
public enum SimplePropertyType implements PropertyType {
    UNKNOWN(Void.class),
    PASSWORD(Password.class),
    HEXSTRING(HexString.class),
    NUMBER(BigDecimal.class),
    BOOLEAN(Boolean.class),
    TIMEDURATION(TimeDuration.class),
    TIMEOFDAY(TimeOfDay.class),
    CLOCK(DateAndTimeFactory.class),
    CODETABLE(Calendar.class, true),
    TIMEZONEINUSE(TimeZoneInUse.class, true),
    REFERENCE(DeviceMessageFile.class, true),
    EAN13(Ean13.class),
    EAN18(Ean18.class),
    DATE(Date.class),
    TEXTAREA(LargeStringFactory.class),
    OBISCODE(ObisCode.class),
    READINGTYPE(ReadingType.class),
    LOADPROFILETYPE(LoadProfileType.class, true),
    LOADPROFILE(LoadProfile.class, true),
    LOGBOOK(LogBook.class, true),
    REGISTER(Register.class, true),
    TEXT(StringFactory.class),
    FIRMWAREVERSION(BaseFirmwareVersion.class, true),
    TIMESTAMP(Instant.class),
    USAGEPOINT(UsagePoint.class, true)
    ;

    private Class discriminatorClass;
    private boolean isReference;

    SimplePropertyType(Class valueFactoryClass) {
        this(valueFactoryClass, false);
    }

    SimplePropertyType(Class discriminatorClass, boolean reference) {
        this.isReference = reference;
        this.discriminatorClass = discriminatorClass;
    }

    public boolean isReference() {
        return isReference;
    }

    private boolean matches (ValueFactory valueFactory) {
        if (ValueFactory.class.isAssignableFrom(this.discriminatorClass)) {
            return this.discriminatorClass.isAssignableFrom(valueFactory.getClass());
        }
        else {
            return this.discriminatorClass.isAssignableFrom(valueFactory.getValueType());
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