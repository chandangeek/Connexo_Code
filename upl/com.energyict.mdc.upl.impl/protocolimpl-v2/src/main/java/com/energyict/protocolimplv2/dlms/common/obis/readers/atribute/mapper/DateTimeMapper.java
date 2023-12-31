package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.DateTime;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.util.Date;

public class DateTimeMapper implements AttributeMapper<DateTime> {

    @Override
    public RegisterValue map(AbstractDataType attribute, OfflineRegister offlineRegister) {
        return new RegisterValue(offlineRegister, new Date(attribute.longValue()));
    }

    @Override
    public Class<DateTime> dataType() {
        return DateTime.class;
    }
}
