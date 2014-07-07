package com.energyict.mdc.dynamic;

import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.common.DataVault;
import com.energyict.mdc.common.DataVaultProvider;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:57)
 */
public class EncryptedStringFactory extends AbstractValueFactory<String> {

    private final DataVault dataVault;

    public EncryptedStringFactory() {
        dataVault = DataVaultProvider.instance.get().getKeyVault();
    }

    @Override
    public Class<String> getValueType() {
        return String.class;
    }

    @Override
    public String getDatabaseTypeName () {
        return "varchar2(4000)";
    }

    @Override
    public int getJdbcType() {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public String valueFromDatabase (Object object) throws SQLException {
        return this.valueFromDb((String) object);
    }

    private String valueFromDb(String encodedString) {
        return new String(dataVault.decrypt(encodedString));
    }

    @Override
    public Object valueToDatabase (String string) {
        if (string == null) {
            return null;
        }
        else {
            return this.encrypt(string);
        }
    }

    private String encrypt (String string) {
        return dataVault.encrypt(string.getBytes());
    }

    @Override
    public String fromStringValue(String stringValue) {
        if (stringValue == null) {
            return "";
        }
        else {
            return this.valueFromDb(stringValue);
        }
    }

    @Override
    public String toStringValue(String object) {
        return this.encrypt(object);
    }

    @Override
    public void bind(SqlBuilder builder, String value) {
        if (value != null) {
            builder.addObject(this.encrypt(value));
        }
        else {
            builder.addNull(this.getJdbcType());
        }
    }

    @Override
    public void bind(PreparedStatement statement, int offset, String value) throws SQLException {
        if (value != null) {
            statement.setString(offset, this.encrypt(value));
        }
        else {
            statement.setNull(offset, this.getJdbcType());
        }
    }

}