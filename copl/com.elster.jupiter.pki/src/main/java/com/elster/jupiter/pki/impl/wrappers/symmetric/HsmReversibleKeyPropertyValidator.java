/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.hsm.model.keys.HsmJssKeyType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.impl.MessageSeeds;

import javax.xml.bind.DatatypeConverter;
import java.util.Map;


class HsmReversibleKeyPropertyValidator {

    @NonEmptyString(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.INVALID_VALUE + "}")
    private  final String key;
    @NonEmptyString(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.INVALID_LABEL + "}")
    private  final String label;

    private HsmReversibleKeyPropertyValidator(String key, String label) {
        this.key = key;
        this.label = label;
    }


    public static HsmReversibleKeyPropertyValidator build(Map<String, Object> properties) {

        String lkey =  getPropertyName(properties, HsmProperties.DECRYPTED_KEY.getPropertyName());
        String llabel = getPropertyName(properties,HsmProperties.LABEL.getPropertyName());

        return new HsmReversibleKeyPropertyValidator(lkey, llabel);
    }

    public void validate(DataModel dataModel) {
        Save.UPDATE.validate(dataModel, this);
    }

    public String getLabel() {
        return label;
    }

    public byte[] getKey(HsmJssKeyType hsmJssKeyType) {
        if (key == null) {
            return null;
        }

        switch (hsmJssKeyType) {
            case AUTHENTICATION: return key.getBytes();
        }

        return DatatypeConverter.parseHexBinary(key);
    }

    private static String getPropertyName(Map<String, Object> properties, String propertyName) {
        if (properties.containsKey(propertyName)){
            return (String)properties.get(propertyName);
        }
        return null;
    }

}
