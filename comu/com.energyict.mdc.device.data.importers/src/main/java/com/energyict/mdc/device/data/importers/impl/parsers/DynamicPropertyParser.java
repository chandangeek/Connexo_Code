package com.energyict.mdc.device.data.importers.impl.parsers;

import com.elster.jupiter.nls.Thesaurus;
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
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
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
            } else {
                return Constants.DATE_AND_TIME_FORMAT.parse(stringValue);
            }
        }

        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return Constants.DATE_AND_TIME_PATTERN;
        }
    },
    DATE(DateFactory.class) {
        @Override
        public Object parse(String stringValue) throws ParseException {
            if (stringValue == null || stringValue.isEmpty()) {
                return null;
            } else {
                return Constants.DATE_FORMAT.parse(stringValue);
            }
        }

        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return Constants.DATE_PATTERN;
        }
    },
    TIME_OF_DAY(TimeOfDayFactory.class) {
        @Override
        public Object parse(String value) throws ParseException {
            if (value == null || value.isEmpty()) {
                return new TimeOfDay(0);
            } else {
                return new TimeOfDay(Integer.parseInt(value));
            }
        }

        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return TranslationKeys.INTEGER_FORMAT.getTranslated(thesaurus);
        }
    },
    EAN13(Ean13Factory.class) {
        @Override
        public Object parse(String stringValue) throws ParseException {
            if (stringValue != null) {
                return new Ean13(stringValue);
            } else {
                return null;
            }
        }

        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return "EAN-13";
        }
    },
    EAN18(Ean18Factory.class) {
        @Override
        public Object parse(String stringValue) throws ParseException {
            if (stringValue == null) {
                return null;
            } else {
                return new Ean18(stringValue);
            }
        }

        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return "EAN-18";
        }
    },
    TIME_ZONE(TimeZoneFactory.class) {
        @Override
        public Object parse(String stringValue) throws ParseException {
            if (stringValue == null) {
                return null;
            } else {
                return TimeZone.getTimeZone(stringValue);
            }
        }

        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return TranslationKeys.INTEGER_FORMAT.getTranslated(thesaurus);
        }
    },
    THREE_STATE_FACTORY(ThreeStateFactory.class) {
        @Override
        public Object parse(String stringValue) throws ParseException {
            if (is(stringValue).emptyOrOnlyWhiteSpace()) {
                return null;
            } else if (Boolean.TRUE.toString().equalsIgnoreCase(stringValue.trim())) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }

        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return TranslationKeys.BOOLEAN_FORMAT.getTranslated(thesaurus);
        }
    },
    OBIS_CODE(ObisCodeValueFactory.class) {
        @Override
        public Object parse(String stringValue) throws ParseException {
            if (stringValue == null || stringValue.isEmpty()) {
                return null;
            } else {
                return ObisCode.fromString(stringValue);
            }
        }

        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return TranslationKeys.OBIS_CODE_FORMAT.getTranslated(thesaurus);
        }
    },
    TIME_DURATION(TimeDurationValueFactory.class) {
        @Override
        public Object parse(String value) throws ParseException {
            if (value == null) {
                return null;
            } else {
                return new TimeDuration(Integer.parseInt(value), TimeDuration.TimeUnit.SECONDS.getCode());
            }
        }

        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return TranslationKeys.INTEGER_FORMAT.getTranslated(thesaurus);
        }
    },
    STRING(StringFactory.class) {
        @Override
        public Object parse(String value) throws ParseException {
            return value;
        }

        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return TranslationKeys.STRING_FORMAT.getTranslated(thesaurus);
        }
    },
    LARGE_STRING(LargeStringFactory.class) {
        @Override
        public Object parse(String value) throws ParseException {
            return value;
        }

        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return TranslationKeys.STRING_FORMAT.getTranslated(thesaurus);
        }
    },
    BOOLEAN(BooleanFactory.class) {
        @Override
        public Object parse(String stringValue) throws ParseException {
            if (!is(stringValue).emptyOrOnlyWhiteSpace() && Boolean.TRUE.toString().equalsIgnoreCase(stringValue.trim())) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }

        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return TranslationKeys.BOOLEAN_FORMAT.getTranslated(thesaurus);
        }
    },
    BIG_DECIMAL(BigDecimalFactory.class) {
        @Override
        public Object parse(String stringValue) throws ParseException {
            if (stringValue == null) {
                return null;
            } else if (this.config != null) {
                return new BigDecimalParser(this.config.numberFormat).parse(stringValue);
            } else {
                return new BigDecimal(stringValue);
            }
        }

        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return this.config != null ? this.config.numberFormat.getExample() : TranslationKeys.NUMBER_FORMAT.getTranslated(thesaurus);
        }
    },
    HEX(HexStringFactory.class) {
        @Override
        public Object parse(String stringValue) throws ParseException {
            if (stringValue == null) {
                return null;
            } else {
                return new HexString(stringValue);
            }
        }

        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return TranslationKeys.HEX_STRING_FORMAT.getTranslated(thesaurus);
        }
    };

    private final Class<? extends ValueFactory<?>> clazz;

    PropertiesParserConfig config;

    DynamicPropertyParser(Class<? extends ValueFactory<?>> clazz) {
        this.clazz = clazz;
    }

    public DynamicPropertyParser configure(PropertiesParserConfig config) {
        this.config = config;
        return this;
    }

    public abstract Object parse(String value) throws ParseException;

    public abstract String getExpectedFormat(Thesaurus thesaurus);

    public static Optional<DynamicPropertyParser> of(Class<? extends ValueFactory> clazz) {
        for (DynamicPropertyParser cvsPropertyParser : DynamicPropertyParser.values()) {
            if (cvsPropertyParser.clazz.equals(clazz)) {
                return Optional.of(cvsPropertyParser);
            }
        }
        return Optional.empty();
    }

    private static class Constants {

        public static final String DATE_PATTERN = "yyyy-MM-dd";
        public static final String DATE_AND_TIME_PATTERN = "yyyy-MM-dd HH:mm";

        public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);
        public static final SimpleDateFormat DATE_AND_TIME_FORMAT = new SimpleDateFormat(DATE_AND_TIME_PATTERN);
    }

    public static class PropertiesParserConfig {

        private SupportedNumberFormat numberFormat = SupportedNumberFormat.FORMAT3;//default is 123456789.012

        private PropertiesParserConfig() {
        }

        public static PropertiesParserConfig newConfig() {
            return new PropertiesParserConfig();
        }

        public PropertiesParserConfig withNumberFormat(SupportedNumberFormat numberFormat) {
            this.numberFormat = numberFormat;
            return this;
        }
    }
}
