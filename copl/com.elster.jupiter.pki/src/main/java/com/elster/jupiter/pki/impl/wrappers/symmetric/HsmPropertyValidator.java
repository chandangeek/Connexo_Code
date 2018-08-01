/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.impl.MessageSeeds;

import javax.xml.bind.DatatypeConverter;
import java.util.Map;

class HsmPropertyValidator {

    @HexBinary(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.HEXBINARY_EVEN_LENGTH + "}")
    @HexStringKey(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.INVALID_HEX_VALUE + "}")
    private  final String key;
    private  final String label;

    private HsmPropertyValidator(String key, String label) {
        this.key = key;
        this.label = label;
    }


    public static HsmPropertyValidator build(Map<String, Object> properties) {

        String propertyName = getPropertyName(properties, HsmProperties.DECRYPTED_KEY.getPropertyName());
        String lkey = propertyName == null? null: DatatypeConverter.printHexBinary(propertyName.getBytes());
        String llabel = getPropertyName(properties,HsmProperties.LABEL.getPropertyName());

        return new HsmPropertyValidator(lkey, llabel);

    }

    private static String getPropertyName(Map<String, Object> properties, String propertyName) {
        if (properties.containsKey(propertyName)){
            return (String)properties.get(propertyName);
        }
        return null;
    }

    public void validate(DataModel dataModel) {
        Save.UPDATE.validate(dataModel, this);
    }

    public String getLabel() {
        return label;
    }

    public byte[] getKey() {
        if (key == null) {
            return null;
        }
        return DatatypeConverter.parseHexBinary(key);
    }
}
