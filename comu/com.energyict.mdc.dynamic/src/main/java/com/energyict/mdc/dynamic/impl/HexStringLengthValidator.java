package com.energyict.mdc.dynamic.impl;

import com.energyict.mdc.common.HexString;

/**
 * Copyrights EnergyICT
 * Date: 10/08/2016
 * Time: 14:38
 */
public class HexStringLengthValidator implements PropertyValidator<HexString> {

    Integer validLength;
    Integer maxLength;

    AbstractEncryptedValueFactory valueFactory;

    public HexStringLengthValidator(){
    }

    HexStringLengthValidator withMaximumLength(int length){
        this.maxLength = length;
        return this;
    }

    HexStringLengthValidator withLength(int length){
        this.validLength = length;
        return this;
    }

    void setValuefactory(AbstractEncryptedValueFactory valueFactory){
        this.valueFactory = valueFactory;
    }

    @Override
    public boolean validate(HexString value) {
        if (validLength == null && maxLength == null){
            throw new IllegalStateException("no length parameters set");
        }
        if (!((validLength != null && value.lenght() == validLength) || (maxLength!= null && value.lenght()<= maxLength))){
            if (valueFactory != null){
                Integer referenceValue;
                if (validLength != null && value.lenght() != validLength){
                    referenceValue = validLength;
                }else{
                    referenceValue = maxLength;
                }
                valueFactory.setReferenceValue(referenceValue);
                valueFactory.setInvalidMessageSeed(MessageSeeds.INVALID_HEX_LENGTH);
            }
            return false;
        }
        return true;
    }
}
