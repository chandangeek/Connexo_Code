package com.energyict.mdc.device.data.importers.impl.properties;

import com.elster.jupiter.properties.CanFindByStringKey;
import com.elster.jupiter.properties.HasIdAndName;

import java.util.Arrays;
import java.util.Optional;

import static com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImporterFactory.COMMA;
import static com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImporterFactory.DOT;

public enum SupportedNumberFormat {

    FORMAT1(DOT, COMMA, "123,456,789.012"),
    FORMAT2(COMMA, DOT, "123.456.789,012"),
    FORMAT3(DOT, "123456789.012"),
    FORMAT4(COMMA, "123456789,012");

    String decimalSeparator;
    String groupSeparator;
    String example;

    SupportedNumberFormat(String decimalSeparator, String example) {
        this.decimalSeparator = decimalSeparator;
        this.example = example;
    }

    SupportedNumberFormat(String decimalSeparator, String groupSeparator, String example) {
        this(decimalSeparator, example);
        this.groupSeparator = groupSeparator;
    }

    public String getDecimalSeparator() {
        return decimalSeparator;
    }

    public String getGroupSeparator() {
        return groupSeparator;
    }

    public String getExample() {
        return example;
    }

    public static SupportedNumberFormatInfo[] valuesAsInfo() {
        return Arrays.asList(values()).stream().map(SupportedNumberFormatInfo::new).toArray(SupportedNumberFormatInfo[]::new);
    }

    public static class SupportedNumberFormatFinder implements CanFindByStringKey<SupportedNumberFormatInfo> {

        @Override
        public Optional<SupportedNumberFormatInfo> find(String key) {
            return Arrays.asList(values()).stream().filter(format -> format.name().equalsIgnoreCase(key)).findFirst().map(SupportedNumberFormatInfo::new);
        }

        @Override
        public Class<SupportedNumberFormatInfo> valueDomain() {
            return SupportedNumberFormatInfo.class;
        }
    }

    public static class SupportedNumberFormatInfo extends HasIdAndName {

        SupportedNumberFormat format;

        SupportedNumberFormatInfo(SupportedNumberFormat format) {
            this.format = format;
        }

        public SupportedNumberFormat getFormat() {
            return format;
        }

        @Override
        public Object getId() {
            return format.name();
        }

        @Override
        public String getName() {
            return format.getExample();
        }
    }
}
