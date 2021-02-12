package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

@RunWith(MockitoJUnitRunner.class)
public class PerTypeMapperTest {

    @Mock
    private RegisterValue registerValue;
    @Mock
    private AttributeMapper<AbstractDataType> defaulAttributeMapper;
    @Mock
    private AttributeMapper<AbstractDataType> specificAttributeMapper;
    @Mock
    private AbstractDataType dataType;
    @Mock
    private ObisCode obisCode;
    @Mock
    private OfflineRegister offlineRegister;

    @Test
    public void emptyList() throws MappingException {
        PerTypeMapper perTypeMapper = new PerTypeMapper(defaulAttributeMapper, new ArrayList<>());
        Mockito.when(defaulAttributeMapper.map(dataType, offlineRegister)).thenReturn(registerValue);
        Assert.assertEquals(registerValue, perTypeMapper.map(dataType, offlineRegister));
        Assert.assertEquals(AbstractDataType.class, perTypeMapper.dataType());
    }

    @Test
    public void dataTypeInList() throws MappingException {
        ArrayList<AttributeMapper<? extends AbstractDataType>> mappers = new ArrayList<>();
        mappers.add(specificAttributeMapper);
        PerTypeMapper perTypeMapper = new PerTypeMapper(defaulAttributeMapper, mappers);
        Mockito.<Class<? extends AbstractDataType>>when(specificAttributeMapper.dataType()).thenReturn(Integer32.class);
        Integer32 attribute = new Integer32(1);
        Mockito.when(specificAttributeMapper.map(attribute, offlineRegister)).thenReturn(registerValue);
        Assert.assertEquals(registerValue, perTypeMapper.map(attribute, offlineRegister));
        Assert.assertEquals(AbstractDataType.class, perTypeMapper.dataType());
    }

    @Test
    public void dataTypeNotInList() throws MappingException {
        ArrayList<AttributeMapper<? extends AbstractDataType>> mappers = new ArrayList<>();
        mappers.add(specificAttributeMapper);
        mappers.add(specificAttributeMapper);
        PerTypeMapper perTypeMapper = new PerTypeMapper(defaulAttributeMapper, mappers);
        Mockito.<Class<? extends AbstractDataType>>when(specificAttributeMapper.dataType()).thenReturn(BitString.class);
        Integer32 attribute = new Integer32(1);
        Mockito.when(defaulAttributeMapper.map(attribute, offlineRegister)).thenReturn(registerValue);
        Assert.assertEquals(registerValue, perTypeMapper.map(attribute, offlineRegister));
        Assert.assertEquals(AbstractDataType.class, perTypeMapper.dataType());
    }

}