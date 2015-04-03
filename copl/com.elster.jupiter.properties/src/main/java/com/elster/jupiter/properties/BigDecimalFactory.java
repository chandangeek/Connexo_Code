package com.elster.jupiter.properties;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.sql.SqlBuilder;
import org.osgi.service.component.annotations.Component;

/**
 * Provides an implementation for the {@link ValueFactory} interface
 * for BigDecimal values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (16:47)
 */
public class BigDecimalFactory extends AbstractValueFactory<BigDecimal> {

    @Override
    public Class<BigDecimal> getValueType () {
        return BigDecimal.class;
    }

    @Override
    public String getDatabaseTypeName () {
        return "number";
    }

    @Override
    public int getJdbcType () {
        return java.sql.Types.NUMERIC;
    }

    @Override
    public BigDecimal valueFromDatabase (Object object) throws SQLException {
        return (BigDecimal) object;
    }

    @Override
    public Object valueToDatabase (BigDecimal object) {
        return object;
    }

    @Override
    public BigDecimal fromStringValue (String stringValue) {
        if (stringValue == null) {
            return null;
        }
        else {
            return new BigDecimal(stringValue);
        }
    }

    @Override
    public String toStringValue (BigDecimal object) {
        if (object == null) {
            return "";
        }
        else {
            return object.toString();
        }
    }

    @Override
    public void bind(PreparedStatement statement, int offset, BigDecimal value) throws SQLException {
        if (value != null) {
            statement.setBigDecimal(offset, value);
        }
        else {
            statement.setNull(offset, this.getJdbcType());
        }
    }

}