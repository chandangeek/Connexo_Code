package com.energyict.mdc.device.data.importers.impl.attributes;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ThreeStateFactory;
import com.elster.jupiter.properties.TimeZoneFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.data.importers.impl.parsers.BigDecimalParser;
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
import org.joda.time.DateTimeConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

import static com.elster.jupiter.util.Checks.is;

public enum DynamicPropertyConverter {
    DATE_AND_TIME(DateAndTimeFactory.class) {
        @Override
        public String convert(String stringValue) throws ParseException {
            if (stringValue == null || stringValue.isEmpty()) {
                return stringValue;
            } else {
                return String.valueOf(Constants.DATE_AND_TIME_FORMAT.parse(stringValue).getTime() / DateTimeConstants.MILLIS_PER_SECOND);
            }
        }

        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return Constants.DATE_AND_TIME_PATTERN;
        }
    },
    DATE(DateFactory.class) {
        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return Constants.DATE_PATTERN;
        }
    },
    TIME_OF_DAY(TimeOfDayFactory.class) {
        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return TranslationKeys.INTEGER_FORMAT.getTranslated(thesaurus);
        }
    },
    EAN13(Ean13Factory.class) {
        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return "EAN-13";
        }
    },
    EAN18(Ean18Factory.class) {
        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return "EAN-18";
        }
    },
    TIME_ZONE(TimeZoneFactory.class) {
        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return TranslationKeys.INTEGER_FORMAT.getTranslated(thesaurus);
        }
    },
    THREE_STATE_FACTORY(ThreeStateFactory.class) {
        @Override
        public String convert(String stringValue) throws ParseException {
            if (is(stringValue).emptyOrOnlyWhiteSpace()) {
                return null;
            } else if (Boolean.TRUE.toString().equalsIgnoreCase(stringValue.trim())) {
                return "1";
            } else {
                return "0";
            }
        }

        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return TranslationKeys.BOOLEAN_FORMAT.getTranslated(thesaurus);
        }
    },
    OBIS_CODE(ObisCodeValueFactory.class) {
        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return TranslationKeys.OBIS_CODE_FORMAT.getTranslated(thesaurus);
        }
    },
    TIME_DURATION(TimeDurationValueFactory.class) {
        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return TranslationKeys.INTEGER_FORMAT.getTranslated(thesaurus);
        }
    },
    STRING(StringFactory.class) {
        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return TranslationKeys.STRING_FORMAT.getTranslated(thesaurus);
        }
    },
    LARGE_STRING(LargeStringFactory.class) {
        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return TranslationKeys.STRING_FORMAT.getTranslated(thesaurus);
        }
    },
    BOOLEAN(BooleanFactory.class) {
        @Override
        public String convert(String stringValue) throws ParseException {
            if (!is(stringValue).emptyOrOnlyWhiteSpace() && Boolean.TRUE.toString().equalsIgnoreCase(stringValue.trim())) {
                return "1";
            } else {
                return "0";
            }
        }

        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return TranslationKeys.BOOLEAN_FORMAT.getTranslated(thesaurus);
        }
    },
    BIG_DECIMAL(BigDecimalFactory.class) {
        @Override
        public String convert(String stringValue) throws ParseException {
            if (stringValue == null || this.config == null) {
                return stringValue;
            }
            return new BigDecimalParser(this.config.numberFormat).parse(stringValue).toString();
        }

        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return this.config != null ? this.config.numberFormat.getExample() : TranslationKeys.NUMBER_FORMAT.getTranslated(thesaurus);
        }
    },
    HEX(HexStringFactory.class) {
        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return TranslationKeys.HEX_STRING_FORMAT.getTranslated(thesaurus);
        }
    };

    private final Class<? extends ValueFactory<?>> clazz;

    PropertiesConverterConfig config;

    DynamicPropertyConverter(Class<? extends ValueFactory<?>> clazz) {
        this.clazz = clazz;
    }

    public DynamicPropertyConverter configure(PropertiesConverterConfig config) {
        this.config = config;
        return this;
    }

    public String convert(String value) throws ParseException {
        return value;
    }

    public abstract String getExpectedFormat(Thesaurus thesaurus);

    public static Optional<DynamicPropertyConverter> of(Class<? extends ValueFactory> clazz) {
        for (DynamicPropertyConverter cvsPropertyParser : DynamicPropertyConverter.values()) {
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

    public static class PropertiesConverterConfig {

        private SupportedNumberFormat numberFormat = SupportedNumberFormat.FORMAT3;//default is 123456789.012

        private PropertiesConverterConfig() {
        }

        public static PropertiesConverterConfig newConfig() {
            return new PropertiesConverterConfig();
        }

        public PropertiesConverterConfig withNumberFormat(SupportedNumberFormat numberFormat) {
            this.numberFormat = numberFormat;
            return this;
        }
    }
}
