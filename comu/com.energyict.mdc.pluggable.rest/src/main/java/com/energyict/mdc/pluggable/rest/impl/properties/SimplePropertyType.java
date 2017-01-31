/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl.properties;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
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
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.DeviceMessageFile;
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;
import com.energyict.mdc.protocol.api.timezones.TimeZoneInUse;

import java.util.Date;

public enum SimplePropertyType implements PropertyType {
    PASSWORD(Password.class),
    HEXSTRING(HexString.class),
    TIMEDURATION(TimeDuration.class),
    TIMEOFDAY(TimeOfDay.class),
    CLOCK(DateAndTimeFactory.class),
    CODETABLE(Calendar.class, true),
    TIMEZONEINUSE(TimeZoneInUse.class, true),
    REFERENCE(DeviceMessageFile.class, true),
    EAN13(Ean13.class),
    EAN18(Ean18.class),
    OBISCODE(ObisCode.class),
    READINGTYPE(ReadingType.class),
    LOADPROFILETYPE(LoadProfileType.class, true),
    LOADPROFILE(LoadProfile.class, true),
    LOGBOOK(LogBook.class, true),
    REGISTER(Register.class, true),
    FIRMWAREVERSION(BaseFirmwareVersion.class, true),
    USAGEPOINT(UsagePoint.class, true),
    DATE(Date.class);

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

}