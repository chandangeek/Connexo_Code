package com.energyict.mdc.dynamic;

import com.energyict.mdc.common.DataVault;
import com.energyict.mdc.common.DataVaultProvider;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Provides an implementation for the {@link ValueFactory}
 * interface for {@link Password}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:12)
 */
public class PasswordFactory extends AbstractValueFactory<Password> {

    private final DataVault dataVault;

    public PasswordFactory () {
        dataVault = DataVaultProvider.instance.get().getKeyVault();
    }

    @Override
    public Class<Password> getValueType () {
        return Password.class;
    }

    @Override
    public String getDatabaseTypeName () {
        return "varchar2(4000)";
    }

    public int getJdbcType () {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public Password valueFromDatabase (Object object) throws SQLException {
        return this.valueFromDb((String) object);
    }

    private Password valueFromDb (String encodedString) {
        return new Password(new String(dataVault.decrypt(encodedString)));
    }

    @Override
    public Object valueToDatabase (Password password) {
        if (password == null) {
            return null;
        }
        else {
            return this.encrypt(password);
        }
    }

    private String encrypt (Password password) {
        String value = password.getValue();
        if (value != null) {
            return dataVault.encrypt(value.getBytes());
        }
        else {
            return null;
        }
    }

    @Override
    public Password fromStringValue (String stringValue) {
        return this.valueFromDb(stringValue);
    }

    @Override
    public String toStringValue (Password object) {
        if (object == null || !(object instanceof Password)) {
            return null;
        }
        else {
            return this.encrypt(object);
        }
    }

    @Override
    public void bind(SqlBuilder builder, Password value) {
        if (value != null) {
            builder.bindString(this.encrypt(value));
        }
        else {
            builder.bindNull(this.getJdbcType());
        }
    }

    @Override
    public void bind(PreparedStatement statement, int offset, Password value) throws SQLException {
        if (value != null) {
            statement.setString(offset, this.encrypt(value));
        }
        else {
            statement.setNull(offset, this.getJdbcType());
        }
    }

}