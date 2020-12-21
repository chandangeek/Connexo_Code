package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.ObisChannel;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;

public class ArrayPositionMapper implements AttributeMapper<Array> {

    private final ObisChannel ignoredChannel;
    private final AttributeMapper<? extends AbstractDataType> actualMapper;

    public ArrayPositionMapper(ObisChannel ignoredChannel, AttributeMapper<? extends AbstractDataType> actualMapper) {
        this.ignoredChannel = ignoredChannel;
        this.actualMapper = actualMapper;
    }

    @Override
    public RegisterValue map(AbstractDataType attribute, ObisCode obisCode) throws MappingException {
        Array array = attribute.getArray();
        int position = ignoredChannel.getValue(obisCode);
        if (attribute.isArray() && array != null) {
            if (position >= array.nrOfDataTypes()) {
                throw new MappingException("Could not read position:" + position + " within array");
            }
            return actualMapper.map(array.getDataType(position), obisCode);
        } else {
            throw new MappingException("Developed obis code:" + obisCode + " as an array yet reading tells otherwise");
        }
    }

    @Override
    public Class<Array> dataType() {
        return Array.class;
    }
}
