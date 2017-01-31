/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dynamic.impl;

import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.properties.HasPropertyValidator;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public abstract class AbstractEncryptedValueFactory<T> extends AbstractValueFactory<T> implements HasPropertyValidator<T> {

    PropertyEncryptor encryptor;
    List<PropertyValidator<T>> validators = new ArrayList<>();
    // MessageSeed used as message for invalid values
    private MessageSeed invalidMessageSeed;

    // The referenceValue on which validation failed
    private Object referenceValue;

    AbstractEncryptedValueFactory(PropertyEncryptor encryptor){
        this.encryptor = encryptor;
    }

    AbstractEncryptedValueFactory<T> addValidator(PropertyValidator<T> validator){
        if (this.validators.add(validator)) {
            if (validator instanceof HexStringLengthValidator) {
                ((HexStringLengthValidator) validator).setValuefactory(this);
            }
        }
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

    protected void setInvalidMessageSeed(MessageSeed seed){
        this.invalidMessageSeed = seed;
    }

    @Override
    public Object getReferenceValue() {
        return referenceValue;
    }

    protected void setReferenceValue(Object referenceValue){
        this.referenceValue = referenceValue;
    }

    @Override
    public MessageSeed invalidMessage() {
        if (invalidMessageSeed == null){
            throw new IllegalStateException("Field 'messageSeed' not 'sown'.");
        }
        return invalidMessageSeed;
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
