package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute;


import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.Matcher;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.CollectedRegisterBuilder;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.helper.ActualAttributeReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper.AttributeMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class AttributeReaderTest {

    @Mock
    private Matcher<?> matcher;
    @Mock
    private CollectedRegisterBuilder collectedRegisterBuilder;
    @Mock
    private AttributeMapper<? extends AbstractDataType> atributeMapper;
    @Mock
    private ActualAttributeReader actualAttributeReader;
    @Mock
    private OfflineRegister offlineRegister;
    @Mock
    private AbstractDlmsProtocol dlmsProtocol;
    @Mock
    private CollectedRegister collectedRegister;
    @Mock
    private RegisterValue registerValue;

    @Test
    public void objectNotInList() throws IOException {
        int attributeNo = 1;
        AttributeReader<?, AbstractDlmsProtocol> attributeReader = new AttributeReader<>(matcher, collectedRegisterBuilder, atributeMapper, attributeNo, actualAttributeReader);
        ObisCode cxoObisCode = ObisCode.fromString("1.2.3.4.5.6");
        ObisCode deviceObisCode = ObisCode.fromString("6.5.4.3.2.1");
        Mockito.when(offlineRegister.getObisCode()).thenReturn(cxoObisCode);
        Mockito.when(matcher.map(cxoObisCode)).thenReturn(deviceObisCode);
        String msg = "msg";
        Mockito.when(actualAttributeReader.read(dlmsProtocol, deviceObisCode, attributeNo)).thenThrow(new NotInObjectListException(msg));
        Mockito.when(collectedRegisterBuilder.createCollectedRegister(offlineRegister, ResultType.InCompatible, msg)).thenReturn(collectedRegister);
        CollectedRegister read = attributeReader.read(dlmsProtocol, offlineRegister);
        Assert.assertEquals(collectedRegister, read);
    }

    @Test
    public void differentMapperType() throws IOException, MappingException {
        int attributeNo = 1;
        AttributeReader<?, AbstractDlmsProtocol> attributeReader = new AttributeReader<>(matcher, collectedRegisterBuilder, atributeMapper, attributeNo, actualAttributeReader);
        ObisCode cxoObisCode = ObisCode.fromString("1.2.3.4.5.6");
        ObisCode deviceObisCode = ObisCode.fromString("6.5.4.3.2.1");
        Mockito.when(offlineRegister.getObisCode()).thenReturn(cxoObisCode);
        Mockito.when(matcher.map(cxoObisCode)).thenReturn(deviceObisCode);
        Integer8 deviceActualRead = new Integer8(8);
        Mockito.when(actualAttributeReader.read(dlmsProtocol, deviceObisCode, attributeNo)).thenReturn(deviceActualRead);
        String msg = "msg";
        Mockito.when(atributeMapper.map(deviceActualRead, offlineRegister)).thenThrow(new MappingException(msg));
        Mockito.when(collectedRegisterBuilder.createCollectedRegister(offlineRegister, ResultType.InCompatible, msg)).thenReturn(collectedRegister);
        CollectedRegister read = attributeReader.read(dlmsProtocol, offlineRegister);
        Assert.assertEquals(collectedRegister, read);
    }

    @Test
    public void allOkUseCase() throws IOException, MappingException {
        int attributeNo = 1;
        AttributeReader<?, AbstractDlmsProtocol> attributeReader = new AttributeReader<>(matcher, collectedRegisterBuilder, atributeMapper, attributeNo, actualAttributeReader);
        ObisCode cxoObisCode = ObisCode.fromString("1.2.3.4.5.6");
        ObisCode deviceObisCode = ObisCode.fromString("6.5.4.3.2.1");
        Mockito.when(offlineRegister.getObisCode()).thenReturn(cxoObisCode);
        Mockito.when(matcher.map(cxoObisCode)).thenReturn(deviceObisCode);
        Integer8 deviceActualRead = new Integer8(8);
        Mockito.when(actualAttributeReader.read(dlmsProtocol, deviceObisCode, attributeNo)).thenReturn(deviceActualRead);
        Mockito.when(atributeMapper.map(deviceActualRead, offlineRegister)).thenReturn(registerValue);
        Mockito.when(collectedRegisterBuilder.createCollectedRegister(offlineRegister, registerValue)).thenReturn(collectedRegister);
        CollectedRegister read = attributeReader.read(dlmsProtocol, offlineRegister);
        Assert.assertEquals(collectedRegister, read);
    }
}