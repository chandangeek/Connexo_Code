package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.ObisChannel;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class should be used only together with DynamicAttributeReader. Should contain a list with mappers for each attribute we want to read
 *
 */
public class DynamicMapper {

    private final List<AttributeMapper<? extends AbstractDataType>> mappers;

    public DynamicMapper(List<AttributeMapper<? extends AbstractDataType>> mappers) {
        this.mappers = mappers;
    }

    public AttributeMapper<? extends AbstractDataType> get(ObisChannel ignoredObisChannel, ObisCode cxoObisCode) throws MappingException {
        // lists starts from 0 while attributes are numbered from 1
        int position = ignoredObisChannel.getValue(cxoObisCode) - 1;
        if (position < 0 || position  >= mappers.size()) {
            throw new MappingException("Configured an ObisCode that exceeds registered mappers for attributes on this ObisCode:" + cxoObisCode + ". Ignored channel (that points to attribute number) is:" + ignoredObisChannel);
        }
        return mappers.get(position);
    }
}
