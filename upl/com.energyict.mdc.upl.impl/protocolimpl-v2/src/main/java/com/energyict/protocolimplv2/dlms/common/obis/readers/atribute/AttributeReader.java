package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.AbstractObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.Matcher;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.CollectedRegisterBuilder;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.helper.ActualAttributeReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper.AttributeMapper;

import java.io.IOException;

public class AttributeReader<T, K extends AbstractDlmsProtocol> extends AbstractObisReader<CollectedRegister, OfflineRegister, T, K> {

    private final CollectedRegisterBuilder collectedRegisterBuilder;
    private final AttributeMapper<? extends AbstractDataType> attributeMapper;
    private final int attributeNumber;
    private final ActualAttributeReader actualAttributeReader;

    public AttributeReader(Matcher<T> matcher, CollectedRegisterBuilder collectedRegisterBuilder, AttributeMapper<? extends AbstractDataType> attributeMapper, int attributeNr) {
        super(matcher);
        this.collectedRegisterBuilder = collectedRegisterBuilder;
        this.attributeMapper = attributeMapper;
        this.attributeNumber = attributeNr;
        actualAttributeReader = new ActualAttributeReader();
    }

    public AttributeReader(Matcher<T> matcher, CollectedRegisterBuilder collectedRegisterBuilder, AttributeMapper<? extends AbstractDataType> attributeMapper, int attributeNr, ActualAttributeReader actualAttributeReader) {
        super(matcher);
        this.collectedRegisterBuilder = collectedRegisterBuilder;
        this.attributeMapper = attributeMapper;
        this.attributeNumber = attributeNr;
        this.actualAttributeReader = actualAttributeReader;
    }

    @Override
    public CollectedRegister read(AbstractDlmsProtocol dlmsProtocol, OfflineRegister offlineRegister) {
        try {
            AbstractDataType read = actualAttributeReader.read(dlmsProtocol, super.map(offlineRegister.getObisCode()), attributeNumber);
            return collectedRegisterBuilder.createCollectedRegister(offlineRegister, attributeMapper.map(read, offlineRegister));
        } catch (MappingException | IOException e) {
            return collectedRegisterBuilder.createCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
        }
    }
}
