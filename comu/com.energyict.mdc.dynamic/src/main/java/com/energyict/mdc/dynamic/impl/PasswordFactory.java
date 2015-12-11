package com.energyict.mdc.dynamic.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.common.Password;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Provides an implementation for the {@link com.elster.jupiter.properties.ValueFactory}
 * interface for {@link Password}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:12)
 */
public class PasswordFactory extends AbstractValueFactory<Password> {

    public static final int MAX_SIZE = 4000;
    private final DataVaultService dataVaultService;

    @Inject
    public PasswordFactory(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }

    @Override
    public Class<Password> getValueType () {
        return Password.class;
    }

    public int getJdbcType () {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public boolean isNull(Password password) {
        return super.isNull(password) || password.getValue() == null || password.getValue().isEmpty();
    }

    @Override
    public Password valueFromDatabase (Object object) {
        return this.valueFromDb((String) object);
    }

    private Password valueFromDb (String encodedString) {
        return new Password(new String(dataVaultService.decrypt(encodedString)));
    }

    @Override
    public Object valueToDatabase (Password password) {
        if (this.isNull(password)) {
            return null;
        }
        else {
            return this.encrypt(password);
        }
    }

    private String encrypt (Password password) {
        String value = password.getValue();
        if (value != null) {
            return dataVaultService.encrypt(value.getBytes());
        }
        else {
            return null;
        }
    }

    @Override
    public Password fromStringValue(String stringValue) {
        return new Password(stringValue);
    }

    @Override
    public String toStringValue(Password object) {
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
            builder.addObject(this.encrypt(value));
        }
        else {
            builder.addNull(this.getJdbcType());
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

    @Override
    public boolean isValid(Password value) {
        String encryptedValue = this.encrypt(value);
        return encryptedValue.length() <= MAX_SIZE;
    }

}