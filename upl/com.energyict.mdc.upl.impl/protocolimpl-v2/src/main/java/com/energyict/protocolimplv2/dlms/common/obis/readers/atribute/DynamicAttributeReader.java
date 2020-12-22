package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.AbstractObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.IgnoreChannelMatcher;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.ObisChannel;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.CollectedRegisterBuilder;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.helper.ActualAttributeReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper.AttributeMapper;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper.DynamicMapper;

import java.io.IOException;

public class DynamicAttributeReader extends AbstractObisReader<CollectedRegister, OfflineRegister, ObisCode> {

    private final ObisChannel ignoredObisChannel;
    private final CollectedRegisterBuilder collectedRegisterBuilder;
    private final DynamicMapper dynamicMapper;
    private final ActualAttributeReader actualAttributeReader;

    public DynamicAttributeReader(IgnoreChannelMatcher matcher, CollectedRegisterBuilder collectedRegisterBuilder, DynamicMapper dynamicMapper) {
        super(matcher);
        this.ignoredObisChannel = matcher.getIgnoredObisChannel();
        this.dynamicMapper = dynamicMapper;
        this.collectedRegisterBuilder = collectedRegisterBuilder;
        actualAttributeReader = new ActualAttributeReader();
    }

    // this constructor adds actual attribute reader, this is used only for test for the moment
    public DynamicAttributeReader(IgnoreChannelMatcher matcher, CollectedRegisterBuilder collectedRegisterBuilder, DynamicMapper dynamicMapper, ActualAttributeReader actualAttributeReader) {
        super(matcher);
        this.ignoredObisChannel = matcher.getIgnoredObisChannel();
        this.dynamicMapper = dynamicMapper;
        this.collectedRegisterBuilder = collectedRegisterBuilder;
        this.actualAttributeReader = actualAttributeReader;
    }

    @Override
    public CollectedRegister read(AbstractDlmsProtocol dlmsProtocol, OfflineRegister offlineRegister) {
        try {
            ObisCode cxoObisCode = offlineRegister.getObisCode();
            AbstractDataType read = actualAttributeReader.read(dlmsProtocol, super.map(cxoObisCode), ignoredObisChannel.getValue(cxoObisCode));
            AttributeMapper<? extends AbstractDataType> mapper = this.dynamicMapper.get(ignoredObisChannel, cxoObisCode);
            return collectedRegisterBuilder.createCollectedRegister(offlineRegister, mapper.map(read, cxoObisCode));
        } catch (MappingException | IOException e) {
            return collectedRegisterBuilder.createCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
        }
    }

}
