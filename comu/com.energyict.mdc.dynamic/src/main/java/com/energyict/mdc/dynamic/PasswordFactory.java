package com.energyict.mdc.dynamic;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.common.Password;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.inject.Inject;

/**
 * Provides an implementation for the {@link ValueFactory}
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

    @Override
    public String getDatabaseTypeName () {
        return "varchar2(" + MAX_SIZE + ")";
    }

    public int getJdbcType () {
        return java.sql.Types.VARCHAR;
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

    public void validate (Password value, String propertyName) throws InvalidValueException {
        String encryptedValue = this.encrypt(value);
        if (encryptedValue.length() > MAX_SIZE) {
            throw new InvalidValueException("XisToBig", "The value \"{0}\" is too large for this property (max length=4000)", propertyName);
        }
    }

}