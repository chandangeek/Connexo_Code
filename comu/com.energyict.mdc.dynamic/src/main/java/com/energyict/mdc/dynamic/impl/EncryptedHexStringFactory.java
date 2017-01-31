/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dynamic.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.energyict.mdc.common.HexString;

import javax.inject.Inject;

public class EncryptedHexStringFactory extends AbstractEncryptedValueFactory<HexString> {

    @Inject
    public EncryptedHexStringFactory(DataVaultService dataVaultService) {
        super(new PropertyEncryptor(dataVaultService));
        this.addValidator(new HexStringValidator());
    }

    @Override
    public Class<HexString> getValueType () {
        return HexString.class;
    }

    @Override
    public boolean isNull(HexString hexString) {
        return super.isNull(hexString) || hexString.isEmpty();
    }

    @Override
    public HexString valueFromDatabase (Object object) {
        String encodedString = (String) object;
        return new HexString(new String(getDecryptedValueFromDatabase(encodedString)));
    }

    @Override
    public HexString fromStringValue(String stringValue) {
        HexString result;
        try{
            result = new HexString(stringValue);
        }catch (IllegalArgumentException ex){
            result = new InvalidHexString(stringValue);
        }
        return result;
    }

    private class HexStringValidator implements PropertyValidator<HexString>{
        @Override
        public boolean validate(HexString value) {
            if (!value.isValid()) {
                setInvalidMessageSeed(MessageSeeds.INVALID_HEX_CHARACTERS);
                setReferenceValue(value);
                return false;
            }
            return true;
        }
    }
    // HexStrings with an invalid content can not be created: an IllegalArgumentException is thrown
    // Nevertheless the UI must point the fact the value is invalid!!!
    private class InvalidHexString extends HexString{
        InvalidHexString(String hexString){
            super();
            setContent(hexString);
        }

        @Override
        public boolean isValid() {
            return false;
        }
    }

}
