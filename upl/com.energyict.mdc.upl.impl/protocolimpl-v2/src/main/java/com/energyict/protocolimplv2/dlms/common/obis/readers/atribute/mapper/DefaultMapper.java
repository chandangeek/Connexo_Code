package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.protocol.RegisterValue;


public class DefaultMapper implements AttributeMapper<AbstractDataType>{

    @Override
    public RegisterValue map(AbstractDataType attribute, OfflineRegister obisCode) {
        return new RegisterValue(obisCode, attribute.toString());
    }

    @Override
    public Class<AbstractDataType> dataType() {
        return AbstractDataType.class;
    }
}
