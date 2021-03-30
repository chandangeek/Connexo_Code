package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;

public class BooleanMapper implements AttributeMapper<BooleanObject> {

    @Override
    public RegisterValue map(AbstractDataType attribute, OfflineRegister offlineRegister) throws MappingException {
        BooleanObject booleanObject = attribute.getBooleanObject();
        if (attribute.isBooleanObject() && booleanObject != null) {
            // this is unsafe but this is what we have...
            return new RegisterValue(offlineRegister, booleanObject.toString());
        } else {
            throw new MappingException("Developed obis code:" + offlineRegister.getObisCode() + " as a boolean yet reading tells otherwise");
        }
    }

    @Override
    public Class<BooleanObject> dataType() {
        return BooleanObject.class;
    }
}
