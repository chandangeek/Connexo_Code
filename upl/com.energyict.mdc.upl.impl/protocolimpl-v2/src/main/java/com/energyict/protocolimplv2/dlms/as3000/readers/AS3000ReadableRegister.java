package com.energyict.protocolimplv2.dlms.as3000.readers;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.as3000.AS3000;
import com.energyict.protocolimplv2.dlms.as3000.readers.register.AS3000BillingRegister;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;
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
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper.custom.OctetStringHexMapper;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.CollectedRegisterBuilder;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.DefaultExtendedRegister;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.DefaultRegister;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedRegisterReader;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedRegisterReaderFinder;
import com.energyict.protocolimplv2.dlms.common.readers.ReaderRegistry;

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

    public CollectedRegisterReader<AS3000> getRegistryReader(AS3000 dlmsProtocol) {
        List<ObisReader<CollectedRegister, OfflineRegister, ObisCode, AS3000>> registers = new ArrayList<>();
        registers.add(dateTime());
        registers.add(firmwareVersion());
        registers.add(specialDayActive());
        registers.add(specialDayPassive());
        registers.add(activityCalendar());
        registers.add(disconnectUnit());
        registers.add(optionBoardRelay1());
        registers.add(optionBoardRelay2());
        registers.add(billingActiveEnergyExport());
        registers.add(billingActiveEnergyImport());
        ReaderRegistry<CollectedRegister, OfflineRegister, ObisCode, AS3000> readableRegisters = new ReaderRegistry<>(registers);
        List<ObisReader<CollectedRegister, OfflineRegister, DLMSClassId, AS3000>> dataClassRegisters = new ArrayList<>();
        dataClassRegisters.add(class1Readers());
        dataClassRegisters.add(class3Readers());
        dataClassRegisters.add(class4Readers());
        ReaderRegistry<CollectedRegister, OfflineRegister, DLMSClassId, AS3000> dataClassReadableRegisters = new ReaderRegistry<>(dataClassRegisters);
        return new CollectedRegisterReader<>(new CollectedRegisterReaderFinder<>(readableRegisters, dataClassReadableRegisters), dlmsProtocol, collectedRegisterBuilder);
    }

    private AttributeReader<ObisCode, AS3000> dateTime() {
        ObisCodeMatcher obisCodeMatcher = new ObisCodeMatcher(ObisCode.fromString("1.1.0.9.2.255"));
        return new AttributeReader<>(obisCodeMatcher, collectedRegisterBuilder, new OctetStringDateTimeMapper(timeZone), 2);
    }

    private AS3000BillingRegister<ObisCode, AS3000> billingActiveEnergyImport() {
        return new AS3000BillingRegister<>(new IgnoreChannelMatcher(ObisCode.fromString("1.1.1.8.0.1"), ObisChannel.F, 255), collectedRegisterBuilder);
    }

    private AS3000BillingRegister<ObisCode, AS3000> billingActiveEnergyExport() {
        return new AS3000BillingRegister<>(new IgnoreChannelMatcher(ObisCode.fromString("1.1.2.8.0.1"), ObisChannel.F, 255), collectedRegisterBuilder);
    }

    private AttributeReader<ObisCode, AS3000> firmwareVersion() {
        ObisCodeMatcher obisCodeMatcher = new ObisCodeMatcher(ObisCode.fromString("1.1.0.2.0.255"));
        return new AttributeReader<>(obisCodeMatcher, collectedRegisterBuilder, new OctetStringHexMapper(), 2);
    }

    private AttributeReader<ObisCode, AS3000> specialDayActive() {
        ObisCodeMatcher obisCodeMatcher = new ObisCodeMatcher(ObisCode.fromString("0.0.11.0.0.255"));
        return new AttributeReader<>(obisCodeMatcher, collectedRegisterBuilder, new DefaultMapper(), 2);
    }

    private AttributeReader<ObisCode, AS3000> specialDayPassive() {
        ObisCodeMatcher obisCodeMatcher = new ObisCodeMatcher(ObisCode.fromString("0.0.171.0.0.255"));
        return new AttributeReader<>(obisCodeMatcher, collectedRegisterBuilder, new DefaultMapper(), 2);
    }

    private DynamicAttributeReader<AS3000> activityCalendar() {
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
        return new DynamicAttributeReader<>(new IgnoreChannelMatcher(ObisCode.fromString("0.0.13.0.0.255"), ObisChannel.E),
                collectedRegisterBuilder, attributeMapper);
    }

    private DynamicAttributeReader<AS3000> disconnectUnit() {
        List<AttributeMapper<? extends AbstractDataType>> mappers = new ArrayList<>();
        mappers.add(new OctetStringMapper());
        mappers.add(new BooleanMapper());
        mappers.add(new DefaultMapper());
        mappers.add(new DefaultMapper());
        DynamicMapper attributeMapper = new DynamicMapper(mappers);
        return new DynamicAttributeReader<>(new IgnoreChannelMatcher(ObisCode.fromString("0.0.96.3.10.255"), ObisChannel.A),
                collectedRegisterBuilder, attributeMapper);
    }

    private DynamicAttributeReader<AS3000> optionBoardRelay1() {
        List<AttributeMapper<? extends AbstractDataType>> mappers = new ArrayList<>();
        mappers.add(new OctetStringMapper());
        mappers.add(new BooleanMapper());
        mappers.add(new DefaultMapper());
        mappers.add(new DefaultMapper());
        DynamicMapper attributeMapper = new DynamicMapper(mappers);
        return new DynamicAttributeReader<>(new IgnoreChannelMatcher(ObisCode.fromString("0.1.96.3.10.255"), ObisChannel.A),
                collectedRegisterBuilder, attributeMapper);
    }

    private DynamicAttributeReader<AS3000> optionBoardRelay2() {
        List<AttributeMapper<? extends AbstractDataType>> mappers = new ArrayList<>();
        mappers.add(new OctetStringMapper());
        mappers.add(new BooleanMapper());
        mappers.add(new DefaultMapper());
        mappers.add(new DefaultMapper());
        DynamicMapper attributeMapper = new DynamicMapper(mappers);
        return new DynamicAttributeReader<>(new IgnoreChannelMatcher(ObisCode.fromString("0.2.96.3.10.255"), ObisChannel.A),
                collectedRegisterBuilder, attributeMapper);
    }

    private AttributeReader<DLMSClassId, AS3000> class1Readers() {
        GenricMatcher<DLMSClassId> matcher = new DlmsClassIdMatcher(DLMSClassId.DATA);
        return new AttributeReader<>(matcher, collectedRegisterBuilder, new PerTypeMapper(), DataAttributes.VALUE.getAttributeNumber());
    }

    private DefaultRegister<DLMSClassId, AS3000> class3Readers() {
        GenricMatcher<DLMSClassId> matcher = new DlmsClassIdMatcher(DLMSClassId.REGISTER);
        return new DefaultRegister<>(matcher, collectedRegisterBuilder);
    }

    private DefaultExtendedRegister<DLMSClassId, AS3000> class4Readers() {
        GenricMatcher<DLMSClassId> matcher = new DlmsClassIdMatcher(DLMSClassId.EXTENDED_REGISTER);
        return new DefaultExtendedRegister<>(matcher, collectedRegisterBuilder, true);
    }

}
