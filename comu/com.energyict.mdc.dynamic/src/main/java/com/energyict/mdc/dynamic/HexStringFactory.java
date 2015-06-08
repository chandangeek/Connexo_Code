package com.energyict.mdc.dynamic;

import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.common.HexString;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:01)
 */
public class HexStringFactory extends AbstractValueFactory<HexString> {

    public static final int MAX_SIZE = 4000;

    @Override
    public Class<HexString> getValueType () {
        return HexString.class;
    }

    @Override
    public String getDatabaseTypeName () {
        return "varchar2(" + MAX_SIZE + ")";
    }

    @Override
    public int getJdbcType () {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public HexString valueFromDatabase (Object object) {
        if (object == null) {
            return null;
        }
        else {
            return new HexString((String) object);
        }
    }

    @Override
    public Object valueToDatabase (HexString object) {
        if (object == null) {
            return null;
        }
        else {
            return this.toStringValue(object);
        }
    }

    @Override
    public HexString fromStringValue (String stringValue) {
        if (stringValue == null) {
            return null;
        }
        else {
            return new HexString(stringValue);
        }
    }

    @Override
    public String toStringValue (HexString object) {
        if (object == null) {
            return "";
        }
        else {
            return object.toString();
        }
    }

    @Override
    public void bind(SqlBuilder builder, HexString value) {
        if (value != null) {
            builder.addObject(this.toStringValue(value));
        }
        else {
            builder.addNull(this.getJdbcType());
        }
    }

    @Override
    public void bind(PreparedStatement statement, int offset, HexString value) throws SQLException {
        if (value != null) {
            statement.setString(offset, this.toStringValue(value));
        }
        else {
            statement.setNull(offset, this.getJdbcType());
        }
    }

    public void validate (String value, String propertyName) throws InvalidValueException {
        if (value.length() > MAX_SIZE) {
            throw new InvalidValueException("XisToBig", "The value \"{0}\" is too large for this property (max length=4000)", propertyName);
        }
    }

}