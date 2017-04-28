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
        return ValueType.fromUPLClassName(this.actual.getValueTypeName()).getConnexoClass();
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
        return ValueType.fromUPLClassName(this.actual.getValueTypeName()).getConnexoSqlType();
    }

}