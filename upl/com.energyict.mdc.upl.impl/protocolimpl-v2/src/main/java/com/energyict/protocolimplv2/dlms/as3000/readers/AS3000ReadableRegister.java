package com.energyict.protocolimplv2.dlms.as3000.readers;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.DlmsClassIdMatcher;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.GenricMatcher;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.IgnoreChannelMatcher;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.ObisChannel;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.ObisCodeMatcher;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.AttributeReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.DynamicAttributeReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper.AttributeMapper;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper.BooleanMapper;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper.DefaultMapper;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper.DynamicMapper;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper.OctetStringMapper;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper.PerTypeMapper;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper.custom.OctetStringDateTimeMapper;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.CollectedRegisterBuilder;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.DefaultExtendedRegisterClass;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.DefaultRegisterClass;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedRegisterReader;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedRegisterReaderFinder;
import com.energyict.protocolimplv2.dlms.common.readers.DLMSReaderRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class AS3000ReadableRegister {

    private final CollectedRegisterBuilder collectedRegisterBuilder;
    private final TimeZone timeZone;

    public AS3000ReadableRegister(CollectedRegisterBuilder collectedRegisterBuilder, TimeZone timeZone) {
        this.collectedRegisterBuilder = collectedRegisterBuilder;
        this.timeZone = timeZone;
    }

    public CollectedRegisterReader getRegistryReader(AbstractDlmsProtocol dlmsProtocol) {
        DLMSReaderRegistry<CollectedRegister, OfflineRegister, ObisCode> readableRegisters = new DLMSReaderRegistry<>();
        readableRegisters.add(dateTime());
        readableRegisters.add(specialDayActive());
        readableRegisters.add(specialDayPassive());
        readableRegisters.add(activityCalendar());
        readableRegisters.add(disconnectUnit());
        readableRegisters.add(optionBoardRelay1());
        readableRegisters.add(optionBoardRelay2());
        DLMSReaderRegistry<CollectedRegister, OfflineRegister, DLMSClassId> dataClassReadableRegisters = new DLMSReaderRegistry<>();
        dataClassReadableRegisters.add(class1Readers());
        dataClassReadableRegisters.add(class3Readers());
        dataClassReadableRegisters.add(class4Readers());
        return new CollectedRegisterReader(new CollectedRegisterReaderFinder(readableRegisters, dataClassReadableRegisters), dlmsProtocol, collectedRegisterBuilder);
    }

    private AttributeReader<ObisCode> dateTime() {
        ObisCodeMatcher obisCodeMatcher = new ObisCodeMatcher(ObisCode.fromString("1.1.0.9.2.255"));
        return new AttributeReader<>(obisCodeMatcher, collectedRegisterBuilder, new OctetStringDateTimeMapper(timeZone), 2);
    }

    private AttributeReader<ObisCode> specialDayActive() {
        ObisCodeMatcher obisCodeMatcher = new ObisCodeMatcher(ObisCode.fromString("0.0.11.0.0.255"));
        return new AttributeReader<>(obisCodeMatcher, collectedRegisterBuilder, new DefaultMapper(), 2);
    }

    private AttributeReader<ObisCode> specialDayPassive() {
        ObisCodeMatcher obisCodeMatcher = new ObisCodeMatcher(ObisCode.fromString("0.0.171.0.0.255"));
        return new AttributeReader<>(obisCodeMatcher, collectedRegisterBuilder, new DefaultMapper(), 2);
    }

    private DynamicAttributeReader activityCalendar() {
        List<AttributeMapper<? extends AbstractDataType>> mappers = new ArrayList<>();
        mappers.add(new OctetStringMapper());
        mappers.add(new OctetStringMapper());
        mappers.add(new PerTypeMapper());
        mappers.add(new PerTypeMapper());
        mappers.add(new PerTypeMapper());
        mappers.add(new OctetStringMapper());
        mappers.add(new PerTypeMapper());
        mappers.add(new PerTypeMapper());
        mappers.add(new PerTypeMapper());
        mappers.add(new OctetStringMapper());
        DynamicMapper attributeMapper = new DynamicMapper(mappers);
        return new DynamicAttributeReader(new IgnoreChannelMatcher(ObisCode.fromString("0.0.13.0.0.255"), ObisChannel.E),
                collectedRegisterBuilder, attributeMapper);
    }

    private DynamicAttributeReader disconnectUnit() {
        List<AttributeMapper<? extends AbstractDataType>> mappers = new ArrayList<>();
        mappers.add(new OctetStringMapper());
        mappers.add(new BooleanMapper());
        mappers.add(new DefaultMapper());
        mappers.add(new DefaultMapper());
        DynamicMapper attributeMapper = new DynamicMapper(mappers);
        return new DynamicAttributeReader(new IgnoreChannelMatcher(ObisCode.fromString("0.0.96.3.10.255"), ObisChannel.A),
                collectedRegisterBuilder, attributeMapper);
    }

    private DynamicAttributeReader optionBoardRelay1() {
        List<AttributeMapper<? extends AbstractDataType>> mappers = new ArrayList<>();
        mappers.add(new OctetStringMapper());
        mappers.add(new BooleanMapper());
        mappers.add(new DefaultMapper());
        mappers.add(new DefaultMapper());
        DynamicMapper attributeMapper = new DynamicMapper(mappers);
        return new DynamicAttributeReader(new IgnoreChannelMatcher(ObisCode.fromString("0.1.96.3.10.255"), ObisChannel.A),
                collectedRegisterBuilder, attributeMapper);
    }

    private DynamicAttributeReader optionBoardRelay2() {
        List<AttributeMapper<? extends AbstractDataType>> mappers = new ArrayList<>();
        mappers.add(new OctetStringMapper());
        mappers.add(new BooleanMapper());
        mappers.add(new DefaultMapper());
        mappers.add(new DefaultMapper());
        DynamicMapper attributeMapper = new DynamicMapper(mappers);
        return new DynamicAttributeReader(new IgnoreChannelMatcher(ObisCode.fromString("0.2.96.3.10.255"), ObisChannel.A),
                collectedRegisterBuilder, attributeMapper);
    }

    private AttributeReader<DLMSClassId> class1Readers() {
        GenricMatcher<DLMSClassId> matcher = new DlmsClassIdMatcher(DLMSClassId.DATA);
        return new AttributeReader<>(matcher, collectedRegisterBuilder, new PerTypeMapper(), DataAttributes.VALUE.getAttributeNumber());
    }

    private DefaultRegisterClass<DLMSClassId> class3Readers() {
        GenricMatcher<DLMSClassId> matcher = new DlmsClassIdMatcher(DLMSClassId.REGISTER);
        return new DefaultRegisterClass<>(matcher, collectedRegisterBuilder);
    }

    private DefaultExtendedRegisterClass<DLMSClassId> class4Readers() {
        GenricMatcher<DLMSClassId> matcher = new DlmsClassIdMatcher(DLMSClassId.EXTENDED_REGISTER);
        return new DefaultExtendedRegisterClass<>(matcher, collectedRegisterBuilder);
    }

}
