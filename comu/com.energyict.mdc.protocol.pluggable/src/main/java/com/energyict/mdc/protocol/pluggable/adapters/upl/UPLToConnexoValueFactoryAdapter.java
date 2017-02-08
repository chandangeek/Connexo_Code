package com.energyict.mdc.protocol.pluggable.adapters.upl;

import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Adapter between {@link com.energyict.mdc.upl.properties.ValueFactory upl}
 * and {@link ValueFactory Connexo} value factory interfaces.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-12 (13:43)
 */
public class UPLToConnexoValueFactoryAdapter implements ValueFactory {

    private static final String BOOLEAN = "java.lang.boolean";
    private static final String BOXED_BOOLEAN = "java.lang.Boolean";
    private static final String STRING = "java.lang.String";
    private static final String HEX_STRING = "com.energyict.mdc.upl.properties.HexString";
    private static final String INTEGER = "java.lang.int";
    private static final String LONG = "java.lang.long";
    private static final String BOXED_INTEGER = "java.lang.Integer";
    private static final String BOXED_LONG = "java.lang.Long";
    private static final String BIGDECIMAL = "java.math.BigDecimal";
    private static final String DATE = "java.util.Date";
    private static final String LOCAL_TIME = "java.time.LocalTime";
    private static final String JAVA_TIME_DURATION = "java.time.Duration";
    private static final String TEMPORAL_AMOUNT = "java.time.temporal.TemporalAmount";
    private static final String TIME_ZONE = "java.util.TimeZone";
    private static final String DEVICE_MESSAGE_FILE = "com.energyict.mdc.upl.properties.DeviceMessageFile";
    private static final String DEVICE_GROUP = "com.energyict.mdc.upl.properties.DeviceGroup";
    private static final String LOAD_PROFILE = "com.energyict.mdc.upl.properties.LoadProfile";
    private static final String TARRIFF_CALENDAR = "com.energyict.mdc.upl.properties.TariffCalendar";
    private static final String FIRMWARE_VERSION = "com.energyict.mdc.upl.properties.FirmwareVersion";
    private static final String OBIS_CODE = "com.energyict.obis.ObisCode";

    private final com.energyict.mdc.upl.properties.ValueFactory actual;

    public UPLToConnexoValueFactoryAdapter(com.energyict.mdc.upl.properties.ValueFactory actual) {
        this.actual = actual;
    }

    @Override
    public Object fromStringValue(String stringValue) {
        return this.actual.fromStringValue(stringValue);
    }

    @Override
    public String toStringValue(Object object) {
        return this.actual.toStringValue(object);
    }

    @Override
    public Class getValueType() {
        return ValueType.fromClassName(this.actual.getValueTypeName()).getConnexoClass();
    }

    @Override
    public Object valueFromDatabase(Object object) {
        return this.actual.valueFromDatabase(object);
    }

    @Override
    public Object valueToDatabase(Object object) {
        return this.actual.valueToDatabase(object);
    }

    @Override
    public void bind(PreparedStatement statement, int offset, Object value) throws SQLException {
        if (value != null) {
            statement.setObject(offset, this.actual.valueToDatabase(value));
        } else {
            statement.setNull(offset, this.sqlType());
        }
    }

    @Override
    public void bind(SqlBuilder builder, Object value) {
        if (value == null) {
            builder.addNull(this.sqlType());
        } else {
            builder.addObject(this.actual.valueToDatabase(value));
        }
    }

    private int sqlType() {
        return ValueType.fromClassName(this.actual.getValueTypeName()).getConnexoSqlType();
    }

}