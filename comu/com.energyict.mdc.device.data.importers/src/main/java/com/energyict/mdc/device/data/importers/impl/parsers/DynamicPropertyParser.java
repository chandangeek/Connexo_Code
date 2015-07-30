package com.energyict.mdc.device.data.importers.impl.parsers;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ThreeStateFactory;
import com.elster.jupiter.properties.TimeZoneFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeOfDay;
import com.energyict.mdc.common.ean.Ean13;
import com.energyict.mdc.common.ean.Ean18;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.DateFactory;
import com.energyict.mdc.dynamic.Ean13Factory;
import com.energyict.mdc.dynamic.Ean18Factory;
import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.LargeStringFactory;
import com.energyict.mdc.dynamic.ObisCodeValueFactory;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.dynamic.TimeOfDayFactory;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.TimeZone;

import static com.elster.jupiter.util.Checks.is;

public enum DynamicPropertyParser {
    DATE_AND_TIME(DateAndTimeFactory.class) {
        @Override
        public Object parse(String stringValue) throws ParseException {
            if (stringValue == null || stringValue.isEmpty()) {
                return null;
            }
            else {
                return Constants.DATE_AND_TIME_FORMAT.parse(stringValue);
            }
        }
    },
    DATE(DateFactory.class) {
        @Override
        public Object parse(String stringValue) throws ParseException {
            if (stringValue == null || stringValue.isEmpty()) {
                return null;
            }
            else {
                return Constants.DATE_FORMAT.parse(stringValue);
            }
        }
    },
    TIME_OF_DAY(TimeOfDayFactory.class) {
        @Override
        public Object parse(String value) throws ParseException {
            if (value == null || value.isEmpty()) {
                return new TimeOfDay(0);
            }
            else {
                return new TimeOfDay(Integer.parseInt(value));
            }

        }
    },
    EAN13(Ean13Factory.class) {
        @Override
        public Object parse(String stringValue) throws ParseException {
            if (stringValue!=null) {
                return new Ean13(stringValue);
            } else {
                return null;
            }
        }
    },
    EAN18(Ean18Factory.class) {
        @Override
        public Object parse(String stringValue) throws ParseException {
            if (stringValue == null || stringValue.length() != 18) {
                return null;
            }
            else {
                return new Ean18(stringValue);
            }
        }
    },
    TIME_ZONE(TimeZoneFactory.class) {
        @Override
        public Object parse(String stringValue) throws ParseException {
            if (stringValue == null) {
                return null;
            }
            else {
                return TimeZone.getTimeZone(stringValue);
            }
        }
    },
    THREE_STATE_FACTORY(ThreeStateFactory.class) {
        @Override
        public Object parse(String stringValue) throws ParseException {
            if (   !is(stringValue).emptyOrOnlyWhiteSpace()
                && "1".equals(stringValue.trim())) {
                return Boolean.TRUE;
            }
            else {
                return Boolean.FALSE;
            }
        }
    },
    OBIS_CODE(ObisCodeValueFactory.class) {
        @Override
        public Object parse(String stringValue) throws ParseException {
            if (stringValue == null || stringValue.isEmpty()) {
                return null;
            }
            else {
                return ObisCode.fromString(stringValue);
            }
        }
    },
    TIME_DURATION(TimeDurationValueFactory.class) {
        @Override
        public Object parse(String value) throws ParseException {
            if (value == null) {
                return null;
            }
            else {
                return new TimeDuration(Integer.parseInt(value), TimeDuration.TimeUnit.SECONDS.getCode());
            }

        }
    },
    STRING(StringFactory.class) {
        @Override
        public Object parse(String value) throws ParseException {
            return value;
        }
    },
    LARGE_STRING(LargeStringFactory.class) {
        @Override
        public Object parse(String value) throws ParseException {
            return value;
        }
    },
    BOOLEAN(BooleanFactory.class) {
        @Override
        public Object parse(String stringValue) throws ParseException {
            if (!is(stringValue).emptyOrOnlyWhiteSpace()
                    && "1".equals(stringValue.trim())) {
                return Boolean.TRUE;
            }
            else {
                return Boolean.FALSE;
            }

        }
    },
    BIG_DECIMAL(BigDecimalFactory.class) {
        @Override
        public Object parse(String stringValue) throws ParseException {
            if (stringValue == null) {
                return null;
            }
            else {
                return new BigDecimal(stringValue);
            }

        }
    },
    HEX(HexStringFactory.class) {
        @Override
        public Object parse(String stringValue) throws ParseException {
            if (stringValue == null) {
                return null;
            }
            else {
                return new HexString(stringValue);
            }

        }
    };
    private final Class<? extends ValueFactory<?>> clazz;

    DynamicPropertyParser(Class<? extends ValueFactory<?>> clazz) {
        this.clazz = clazz;
    }

    public abstract Object parse(String value) throws ParseException;

    public static Optional<DynamicPropertyParser> of(Class<? extends ValueFactory> clazz) {
        for (DynamicPropertyParser cvsPropertyParser : DynamicPropertyParser.values()) {
            if (cvsPropertyParser.clazz.equals(clazz)) {
                return Optional.of(cvsPropertyParser);
            }
        }
        return Optional.empty();
    }

    private static class Constants {
        public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
        public static final SimpleDateFormat DATE_AND_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }
}
