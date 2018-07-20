/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.pki.impl.MessageSeeds;

import javax.xml.bind.DatatypeConverter;

class HsmPropertySetter implements PropertySetter{

    @HexBinary(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.HEXBINARY_EVEN_LENGTH + "}")
    @HexStringKey(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.INVALID_HEX_VALUE + "}")
    private String key;
    private String label;

    HsmPropertySetter(HsmSymmetricKeyImpl hsmSymmetricKey) {
        this.key = DatatypeConverter.printHexBinary(hsmSymmetricKey.getKey());
        this.label = hsmSymmetricKey.getKeyLabel();
    }

    @Override
    public void setHexBinaryKey(String key) {
        this.key = key;
    }

    @Override
    public String getHexBinaryKey() {
        return this.key;
    }

    @Override
    public byte[] getKey(){
        return DatatypeConverter.parseHexBinary(this.key);
    }

    void setLabel(String label){
        this.label = label;
    }

    String getLabel(){
        return label;
    }
}
