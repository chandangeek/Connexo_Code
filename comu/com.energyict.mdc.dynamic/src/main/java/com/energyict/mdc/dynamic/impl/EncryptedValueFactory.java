package com.energyict.mdc.dynamic.impl;

import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Copyrights EnergyICT
 * Date: 10/08/2016
 * Time: 12:56
 */
public abstract class EncryptedValueFactory<T> extends AbstractValueFactory<T> {

    PropertyEncryptor encryptor;
    List<PropertyValidator<T>> validators = new ArrayList<>();

    EncryptedValueFactory(PropertyEncryptor encryptor){
        this.encryptor = encryptor;
    }

    EncryptedValueFactory<T> addValidator(PropertyValidator<T> validator){
        this.validators.add(validator);
        return this;
    }

    public int getJdbcType () {
        return java.sql.Types.VARCHAR;
    }

    protected byte[] getDecryptedValueFromDatabase(String encodedString){
        return this.encryptor.decrypt(encodedString);
    }

    @Override
    public Object valueToDatabase(T object) {
        if (object == null)
            return null;
        return encryptor.encrypt(object);
    }

    @Override
    public String toStringValue(T object) {
         return valueToDatabase(object).toString();
    }

    @Override
    public void bind(SqlBuilder builder, T value) {
        if (value != null) {
            builder.addObject(encryptor.encrypt(value));
        }
        else {
            builder.addNull(this.getJdbcType());
        }
    }

    @Override
    public void bind(PreparedStatement statement, int offset, T value) throws SQLException {
        if (value != null) {
            statement.setString(offset, encryptor.encrypt(value));
        }
        else {
            statement.setNull(offset, this.getJdbcType());
        }
    }

    @Override
    public boolean isValid(final T value) {
        return !this.validators.stream().anyMatch(validator -> !validator.validate(value));
    }
}
