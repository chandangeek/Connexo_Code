package com.energyict.mdc.dynamic;

import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:01)
 */
public class HexStringFactory extends AbstractValueFactory<HexString> {

    @Override
    public Class<HexString> getValueType () {
        return HexString.class;
    }

    @Override
    public String getDatabaseTypeName () {
        return "varchar2(4000)";
    }

    @Override
    public int getJdbcType () {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public HexString valueFromDatabase (Object object) throws SQLException {
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
            builder.bindString(this.toStringValue(value));
        }
        else {
            builder.bindNull(this.getJdbcType());
        }
    }

    @Override
    public void bind(PreparedStatement statement, int offset, HexString value) throws SQLException {
        if (value != null) {
            statement.setString(offset,  this.toStringValue(value));
        }
        else {
            statement.setNull(offset, this.getJdbcType());
        }
    }

}