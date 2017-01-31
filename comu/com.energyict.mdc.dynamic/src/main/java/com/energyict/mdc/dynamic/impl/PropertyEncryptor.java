/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dynamic.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.Password;

import javax.inject.Inject;

public class PropertyEncryptor {

    DataVaultService dataVaultService;

    @Inject
    PropertyEncryptor(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }

    public String encrypt (Object object) {
        switch (object.getClass().getSimpleName()){
            case "String":
                return this.encrypt((String) object);
            case "HexString":
                return this.encrypt((HexString) object);
            case "Password":
                return this.encrypt((Password) object );
            default: throw new IllegalArgumentException("Cannot encrypt objects of type "+object.getClass().getName());
        }
    }

    public String encrypt (String string) {
        if (string == null){
            return null;
        }
        return dataVaultService.encrypt(string.getBytes());
    }

    public String encrypt (Password password) {
        if (password == null){
            return null;
        }
        String value = password.getValue();
        if (value != null) {
            return encrypt(value);
        }
        else {
            return null;
        }
    }

    public String encrypt (HexString hex) {
        if (hex == null){
            return null;
        }
        String value = hex.getContent();
        if (value != null) {
            return encrypt(value);
        }
        else {
            return null;
        }
    }

    public byte[] decrypt(String encodedString) {
        return dataVaultService.decrypt(encodedString);
    }



}
