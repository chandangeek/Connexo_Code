package com.energyict.mdc.dynamic.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.orm.Table;

import com.energyict.mdc.common.Password;

import javax.inject.Inject;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:57)
 */
public class EncryptedStringFactory extends EncryptedValueFactory<String> {

    public static final int MAX_SIZE = Table.MAX_STRING_LENGTH;

    @Inject
    public EncryptedStringFactory(DataVaultService dataVaultService) {
        super(new PropertyEncryptor(dataVaultService));
    }

    @Override
    public Class<String> getValueType() {
        return String.class;
    }

    @Override
    public String valueFromDatabase (Object object) {
        return this.valueFromDb((String) object);
    }

    private String valueFromDb(String encodedString) {
        return new String(getDecryptedValueFromDatabase(encodedString));
    }

    @Override
    public String fromStringValue(String stringValue) {
        return stringValue == null? "" : stringValue;
    }

    private class EncryptedStringValidator implements PropertyValidator<String>{
        @Override
        public boolean validate(String value) {
            return ((String) valueToDatabase(value)).length() <= MAX_SIZE;
        }
    }

}