package com.energyict.mdc.device.data.importers.impl.properties;

import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

import static com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImporterFactory.COMMA;
import static com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImporterFactory.DOT;

public enum SupportedNumberFormat {

    FORMAT1(DOT, COMMA, "123,456,789.012"),
    FORMAT2(COMMA, DOT, "123.456.789,012"),
    FORMAT3(DOT, "123456789.012"),
    FORMAT4(COMMA, "123456789,012");

    Character decimalSeparator;
    Character groupSeparator;
    String example;

    SupportedNumberFormat(Character decimalSeparator, String example) {
        this.decimalSeparator = decimalSeparator;
        this.example = example;
    }

    SupportedNumberFormat(Character decimalSeparator, Character groupSeparator, String example) {
        this(decimalSeparator, example);
        this.groupSeparator = groupSeparator;
    }

    public Character getDecimalSeparator() {
        return decimalSeparator;
    }

    public Character getGroupSeparator() {
        return groupSeparator;
    }

    public boolean hasGroupSeparator() {
        return getGroupSeparator() != null;
    }

    public String getExample() {
        return example;
    }

    public static SupportedNumberFormatInfo[] valuesAsInfo() {
        return Arrays.asList(values()).stream().map(SupportedNumberFormatInfo::new).toArray(SupportedNumberFormatInfo[]::new);
    }

    public static class SupportedNumberFormatValueFactory implements ValueFactory<SupportedNumberFormatInfo> {
        @Override
        public SupportedNumberFormatInfo fromStringValue(String stringValue) {
            return Arrays.asList(values()).stream()
                    .filter(format -> format.name().equalsIgnoreCase(stringValue))
                    .findFirst()
                    .map(SupportedNumberFormatInfo::new)
                    .orElse(null);
        }

        @Override
        public String toStringValue(SupportedNumberFormatInfo object) {
            return object.getId();
        }

        @Override
        public Class<SupportedNumberFormatInfo> getValueType() {
            return SupportedNumberFormatInfo.class;
        }

        @Override
        public SupportedNumberFormatInfo valueFromDatabase(Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(SupportedNumberFormatInfo object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(PreparedStatement statement, int offset, SupportedNumberFormatInfo value) throws SQLException {
            if (value != null) {
                statement.setObject(offset, valueToDatabase(value));
            }
            else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, SupportedNumberFormatInfo value) {
            if (value != null) {
                builder.addObject(valueToDatabase(value));
            }
            else {
                builder.addNull(Types.VARCHAR);
            }
        }
    }

    public static class SupportedNumberFormatInfo extends HasIdAndName {

        SupportedNumberFormat format;

        public SupportedNumberFormatInfo(SupportedNumberFormat format) {
            this.format = format;
        }

        public SupportedNumberFormat getFormat() {
            return format;
        }

        @Override
        public String getId() {
            return format.name();
        }

        @Override
        public String getName() {
            return format.getExample();
        }
    }
}
