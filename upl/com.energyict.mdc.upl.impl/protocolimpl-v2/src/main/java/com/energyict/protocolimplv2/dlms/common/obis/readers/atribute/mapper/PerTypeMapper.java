package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;

import java.util.ArrayList;
import java.util.List;

public class PerTypeMapper implements AttributeMapper<AbstractDataType> {

    private final AttributeMapper<? extends AbstractDataType> defaultMapper;
    private final List<AttributeMapper<? extends AbstractDataType>> mappers;

    public PerTypeMapper() {
        this.defaultMapper = new DefaultMapper();
        this.mappers = new ArrayList<>();
        // keep them alphabetically ordered
        mappers.add(new ArrayMapper());
        mappers.add(new BitStringMapper());
        mappers.add(new BooleanMapper());
        mappers.add(new DateTimeMapper());
        mappers.add(new I32Mapper(Unit.getUndefined()));
        mappers.add(new I64Mapper(Unit.getUndefined()));
        mappers.add(new OctetStringMapper());
        mappers.add(new U8Mapper(Unit.getUndefined()));
        mappers.add(new U16Mapper(Unit.getUndefined()));
        mappers.add(new U32Mapper(Unit.getUndefined()));
        mappers.add(new U64Maper(Unit.getUndefined()));
        mappers.add(new VisibleStringMapper());
    }

    public PerTypeMapper(AttributeMapper<? extends AbstractDataType> defaultMapper, List<AttributeMapper<? extends AbstractDataType>> mappers) {
        this.defaultMapper = defaultMapper;
        this.mappers = mappers;
    }

    @Override
    public RegisterValue map(AbstractDataType attribute, ObisCode obisCode) throws MappingException {
        for (AttributeMapper<? extends AbstractDataType> mapper: mappers) {
            if (mapper.dataType().equals(attribute.getClass())) {
                return mapper.map(attribute, obisCode);
            }
        }
        return defaultMapper.map(attribute, obisCode);
    }

    @Override
    public Class<AbstractDataType> dataType() {
        return AbstractDataType.class;
    }
}
