package com.energyict.mdc.protocol.pluggable.adapters.upl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.protocol.api.DeviceMessageFile;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.mdc.upl.properties.FirmwareVersion;
import com.energyict.mdc.upl.properties.HexString;
import com.energyict.obis.ObisCode;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Stream;

/**
 * Models the different ValueTypes that are supported by the
 * {@link UPLToConnexoValueFactoryAdapter} and related classes.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-12 (14:44)
 */
enum ValueType {
    BOOLEAN("java.lang.boolean", boolean.class, Types.INTEGER),
    BOXED_BOOLEAN("java.lang.Boolean", Boolean.class, Types.INTEGER),
    STRING("java.lang.String", String.class, Types.VARCHAR),
    HEX_STRING("com.energyict.mdc.upl.properties.HexString", HexString.class, Types.VARCHAR),
    INTEGER("java.lang.int", int.class, Types.NUMERIC),
    LONG("java.lang.long", long.class, Types.NUMERIC),
    BOXED_INTEGER("java.lang.Integer", Integer.class, Types.NUMERIC),
    BOXED_LONG("java.lang.Long", Long.class, Types.NUMERIC),
    BIGDECIMAL("java.math.BigDecimal", BigDecimal.class, Types.NUMERIC),
    DATE("java.util.Date", Date.class, Types.DATE),
    LOCAL_TIME("java.time.LocalTime", LocalTime.class, Types.NUMERIC),
    JAVA_TIME_DURATION("java.time.Duration", Duration.class, Types.VARCHAR),
    TEMPORAL_AMOUNT("java.time.temporal.TemporalAmount", TemporalAmount.class, Types.VARCHAR),
    TIME_ZONE("java.util.TimeZone", TimeZone.class, Types.VARCHAR),
    DEVICE_MESSAGE_FILE("com.energyict.mdc.upl.properties.DeviceMessageFile", DeviceMessageFile.class, Types.NUMERIC) {
        @Override
        boolean isReference() {
            return true;
        }
    },
    DEVICE_GROUP("com.energyict.mdc.upl.properties.DeviceGroup", EndDeviceGroup.class, Types.NUMERIC) {
        @Override
        boolean isReference() {
            return true;
        }
    },
    LOAD_PROFILE("com.energyict.mdc.upl.properties.LoadProfile", LoadProfile.class, Types.NUMERIC) {
        @Override
        boolean isReference() {
            return true;
        }
    },
    TARRIFF_CALENDAR("com.energyict.mdc.upl.properties.TariffCalendar", Calendar.class, Types.NUMERIC) {
        @Override
        boolean isReference() {
            return true;
        }
    },
    FIRMWARE_VERSION("com.energyict.mdc.upl.properties.FirmwareVersion", FirmwareVersion.class, Types.NUMERIC) {
        @Override
        boolean isReference() {
            return true;
        }
    },
    READING_TYPE("com.energyict.mdc.upl.properties.ReadingType", ReadingType.class, Types.NUMERIC) {
        @Override
        boolean isReference() {
            return true;
        }
    },
    OBIS_CODE("com.energyict.obis.ObisCode", ObisCode.class, Types.VARCHAR);

    private final String className;
    private final Class connexoClass;
    private final int connexoSqlType;

    ValueType(String className, Class connexoClass, int connexoSqlType) {
        this.className = className;
        this.connexoClass = connexoClass;
        this.connexoSqlType = connexoSqlType;
    }

    static ValueType fromClassName(String className) {
        return Stream
                    .of(values())
                    .filter(each -> each.className.equals(className))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Value type " + className + " is not (yet) supported by " + UPLToConnexoValueFactoryAdapter.class.getName()));
    }

    Class getConnexoClass() {
        return connexoClass;
    }

    int getConnexoSqlType() {
        return connexoSqlType;
    }

    boolean isReference() {
        return false;
    }

}