package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;

public interface AttributeMapper<T extends AbstractDataType>  {

    RegisterValue map(AbstractDataType attribute, ObisCode obisCode) throws MappingException;

    Class<T> dataType();
}
