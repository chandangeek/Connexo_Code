package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute;


import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.IgnoreChannelMatcher;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.ObisChannel;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.CollectedRegisterBuilder;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.helper.ActualAttributeReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper.AttributeMapper;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper.DynamicMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class DynamicAttributeReaderTest {

    @Mock
    private CollectedRegisterBuilder collectedRegisterBuilder;
    @Mock
    private DynamicMapper dynamicMapper;
    @Mock
    private ActualAttributeReader actualAttributeReader;
    @Mock
    private OfflineRegister offlineRegister;
    @Mock
    private AbstractDlmsProtocol dlmsProtocol;
    @Mock
    private CollectedRegister collectedRegister;
    @Mock
    private AttributeMapper<? extends AbstractDataType> attributeMapper;
    @Mock
    private AbstractDataType readData;
    @Mock
    private AttributeMapper<? extends AbstractDataType> actualMapper;
    @Mock
    private RegisterValue registerValue;

    @Test
    public void objectNotInList() throws IOException, MappingException {
        ObisCode cxoObisCode = ObisCode.fromString("1.2.3.4.5.6");
        IgnoreChannelMatcher matcher = new IgnoreChannelMatcher(cxoObisCode, ObisChannel.D);
        ObisChannel ignoredChannel = ObisChannel.D;
        ObisCode expectedDeviceObisCode = ignoredChannel.getDeviceValue(cxoObisCode);
        Mockito.when(offlineRegister.getObisCode()).thenReturn(cxoObisCode);
        Mockito.<AttributeMapper<? extends AbstractDataType>>when(dynamicMapper.get(ignoredChannel, cxoObisCode)).thenReturn(attributeMapper);
        String msg = "msg";
        Mockito.when(actualAttributeReader.read(dlmsProtocol, expectedDeviceObisCode, ignoredChannel.getValue(cxoObisCode))).thenThrow(new NotInObjectListException(msg));
        Mockito.when(collectedRegisterBuilder.createCollectedRegister(offlineRegister, ResultType.InCompatible, msg)).thenReturn(collectedRegister);

        DynamicAttributeReader attributeReader = new DynamicAttributeReader(matcher, collectedRegisterBuilder, dynamicMapper, actualAttributeReader);
        CollectedRegister read = attributeReader.read(dlmsProtocol, offlineRegister);
        Assert.assertEquals(collectedRegister, read);
    }

    @Test
    public void noRegisteredAttributeMapper() throws IOException, MappingException {
        ObisCode cxoObisCode = ObisCode.fromString("1.2.3.4.5.6");
        IgnoreChannelMatcher matcher = new IgnoreChannelMatcher(cxoObisCode, ObisChannel.D);
        ObisChannel ignoredChannel = ObisChannel.D;
        ObisCode expectedDeviceObisCode = ignoredChannel.getDeviceValue(cxoObisCode);
        Mockito.when(offlineRegister.getObisCode()).thenReturn(cxoObisCode);
        Mockito.<AttributeMapper<? extends AbstractDataType>>when(dynamicMapper.get(ignoredChannel, cxoObisCode)).thenReturn(attributeMapper);
        String msg = "msg";
        Mockito.when(actualAttributeReader.read(dlmsProtocol, expectedDeviceObisCode, ignoredChannel.getValue(cxoObisCode))).thenReturn(readData);
        Mockito.when(dynamicMapper.get(ignoredChannel, cxoObisCode)).thenThrow(new MappingException(msg));
        Mockito.when(collectedRegisterBuilder.createCollectedRegister(offlineRegister, ResultType.InCompatible, msg)).thenReturn(collectedRegister);

        DynamicAttributeReader attributeReader = new DynamicAttributeReader(matcher, collectedRegisterBuilder, dynamicMapper, actualAttributeReader);
        CollectedRegister read = attributeReader.read(dlmsProtocol, offlineRegister);
        Assert.assertEquals(collectedRegister, read);
    }

    @Test
    public void allOkScenario() throws IOException, MappingException {
        ObisCode cxoObisCode = ObisCode.fromString("1.2.3.4.5.6");
        IgnoreChannelMatcher matcher = new IgnoreChannelMatcher(cxoObisCode, ObisChannel.D);
        ObisChannel ignoredChannel = ObisChannel.D;
        ObisCode expectedDeviceObisCode = ignoredChannel.getDeviceValue(cxoObisCode);
        Mockito.when(offlineRegister.getObisCode()).thenReturn(cxoObisCode);
        Mockito.<AttributeMapper<? extends AbstractDataType>>when(dynamicMapper.get(ignoredChannel, cxoObisCode)).thenReturn(attributeMapper);
        String msg = "msg";
        Mockito.when(actualAttributeReader.read(dlmsProtocol, expectedDeviceObisCode, ignoredChannel.getValue(cxoObisCode))).thenReturn(readData);
        Mockito.<AttributeMapper<? extends AbstractDataType>>when(dynamicMapper.get(ignoredChannel, cxoObisCode)).thenReturn(actualMapper);
        Mockito.when(actualMapper.map(readData, offlineRegister)).thenReturn(registerValue);
        Mockito.when(collectedRegisterBuilder.createCollectedRegister(offlineRegister, registerValue)).thenReturn(collectedRegister);

        DynamicAttributeReader attributeReader = new DynamicAttributeReader(matcher, collectedRegisterBuilder, dynamicMapper, actualAttributeReader);
        CollectedRegister read = attributeReader.read(dlmsProtocol, offlineRegister);
        Assert.assertEquals(collectedRegister, read);
    }

}