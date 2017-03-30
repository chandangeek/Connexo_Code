/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dynamic.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.energyict.mdc.common.Password;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link com.elster.jupiter.properties.ValueFactory}
 * interface for {@link Password}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:12)
 */
public class PasswordFactory extends AbstractEncryptedValueFactory<Password> {

    public static final int MAX_SIZE = 4000;

    @Inject
    public PasswordFactory(DataVaultService dataVaultService) {
        super(new PropertyEncryptor(dataVaultService));
        this.addValidator(new PasswordValidator());
    }

    @Override
    public Class<Password> getValueType () {
        return Password.class;
    }

    @Override
    public boolean isNull(Password password) {
        return super.isNull(password) || password.isEmpty();
    }

    @Override
    public Password valueFromDatabase (Object object) {
        String encodedString = (String) object;
        return new Password(new String(getDecryptedValueFromDatabase(encodedString)));
    }

    @Override
    public Password fromStringValue(String stringValue) {
        return new Password(stringValue);
    }

    private class PasswordValidator implements PropertyValidator<Password>{
        @Override
        public boolean validate(Password value) {
            if (((String) valueToDatabase(value)).length() > MAX_SIZE){
                setReferenceValue(MAX_SIZE);
                setInvalidMessageSeed(MessageSeeds.LENGTH_EXCEEDS_MAXIMUM);
                return false;
            }
            return true;
        }
    }

}