/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
import com.energyict.mdc.dynamic.LocalTimeFactory;
import com.energyict.mdc.dynamic.ObisCodeValueFactory;
import com.energyict.mdc.dynamic.TemporalAmountValueFactory;

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
    TIME_OF_DAY(LocalTimeFactory.class) {
        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return thesaurus.getFormat(TranslationKeys.INTEGER_FORMAT).format();
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
            return thesaurus.getFormat(TranslationKeys.INTEGER_FORMAT).format();
        }
    },
    THREE_STATE_FACTORY(ThreeStateFactory.class) {
        @Override
        public String convert(String stringValue) {
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
            return thesaurus.getFormat(TranslationKeys.BOOLEAN_FORMAT).format();
        }
    },
    OBIS_CODE(ObisCodeValueFactory.class) {
        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return thesaurus.getFormat(TranslationKeys.OBIS_CODE_FORMAT).format();
        }
    },
    TIME_DURATION(TemporalAmountValueFactory.class) {
        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return thesaurus.getFormat(TranslationKeys.INTEGER_FORMAT).format();
        }
    },
    STRING(StringFactory.class) {
        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return thesaurus.getFormat(TranslationKeys.STRING_FORMAT).format();
        }
    },
    LARGE_STRING(LargeStringFactory.class) {
        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return thesaurus.getFormat(TranslationKeys.STRING_FORMAT).format();
        }
    },
    BOOLEAN(BooleanFactory.class) {
        @Override
        public String convert(String stringValue) {
            if (!is(stringValue).emptyOrOnlyWhiteSpace() && Boolean.TRUE.toString().equalsIgnoreCase(stringValue.trim())) {
                return "1";
            } else {
                return "0";
            }
        }

        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return thesaurus.getFormat(TranslationKeys.BOOLEAN_FORMAT).format();
        }
    },
    BIG_DECIMAL(BigDecimalFactory.class) {
        @Override
        public String convert(String stringValue) {
            if (stringValue == null || this.config == null) {
                return stringValue;
            }
            return new BigDecimalParser(this.config.numberFormat).parse(stringValue).toString();
        }

        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return this.config != null ? this.config.numberFormat.getExample() : thesaurus.getFormat(TranslationKeys.NUMBER_FORMAT).format();
        }
    },
    HEX(HexStringFactory.class) {
        @Override
        public String getExpectedFormat(Thesaurus thesaurus) {
            return thesaurus.getFormat(TranslationKeys.HEX_STRING_FORMAT).format();
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
        for (DynamicPropertyConverter propertyConverter : DynamicPropertyConverter.values()) {
            if (propertyConverter.clazz.equals(clazz)) {
                return Optional.of(propertyConverter);
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
