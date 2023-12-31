package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;


import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.Integer64;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.ObisChannel;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ArrayPositionMapperTest {

    @Mock
    private OfflineRegister offlineRegister;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void okScenarioPosition0() throws MappingException {
        Unit undefined = Unit.getUndefined();
        ArrayPositionMapper mapper = new ArrayPositionMapper(ObisChannel.C, new I64Mapper(undefined));
        Integer64 pos0 = new Integer64(1);
        Integer64 pos1 = new Integer64(2);
        AbstractDataType attribute = new Array(pos0, pos1);
        ObisCode obisCode = ObisCode.fromString("0.1.0.3.4.5");
        Mockito.when(offlineRegister.getObisCode()).thenReturn(obisCode);
        RegisterValue registerValue = mapper.map(attribute, offlineRegister);
        Assert.assertEquals(pos0.toBigDecimal(), registerValue.getQuantity().getAmount());
        Assert.assertEquals(undefined, registerValue.getQuantity().getUnit());
    }

    @Test
    public void okScenarioPosition1() throws MappingException {
        Unit undefined = Unit.getUndefined();
        ArrayPositionMapper mapper = new ArrayPositionMapper(ObisChannel.B, new I64Mapper(undefined));
        Integer64 pos0 = new Integer64(1);
        Integer64 pos1 = new Integer64(2);
        AbstractDataType attribute = new Array(pos0, pos1);
        ObisCode obisCode = ObisCode.fromString("0.1.0.3.4.5");
        Mockito.when(offlineRegister.getObisCode()).thenReturn(obisCode);
        RegisterValue registerValue = mapper.map(attribute, offlineRegister);
        Assert.assertEquals(pos1.toBigDecimal(), registerValue.getQuantity().getAmount());
        Assert.assertEquals(undefined, registerValue.getQuantity().getUnit());
    }

    @Test
    public void differentType() throws MappingException {
        expectedException.expect(MappingException.class);
        Unit undefined = Unit.getUndefined();
        ArrayPositionMapper mapper = new ArrayPositionMapper(ObisChannel.B, new I64Mapper(undefined));
        Integer64 pos0 = new Integer64(1);
        Unsigned8 pos1 = new Unsigned8(0);
        AbstractDataType attribute = new Array(pos0, pos1);
        ObisCode obisCode = ObisCode.fromString("0.1.0.3.4.5");
        Mockito.when(offlineRegister.getObisCode()).thenReturn(obisCode);
        mapper.map(attribute, offlineRegister);
    }

    @Test
    public void outSideArrayPosition() throws MappingException {
        expectedException.expect(MappingException.class);
        Unit undefined = Unit.getUndefined();
        ArrayPositionMapper mapper = new ArrayPositionMapper(ObisChannel.F, new I64Mapper(undefined));
        Integer64 pos0 = new Integer64(1);
        Integer64 pos1 = new Integer64(2);
        AbstractDataType attribute = new Array(pos0, pos1);
        ObisCode obisCode = ObisCode.fromString("0.1.0.3.4.2");
        Mockito.when(offlineRegister.getObisCode()).thenReturn(obisCode);
        mapper.map(attribute, offlineRegister);
    }

    @Test
    public void notArray() throws MappingException {
        expectedException.expect(MappingException.class);
        Unit undefined = Unit.getUndefined();
        ArrayPositionMapper mapper = new ArrayPositionMapper(ObisChannel.F, new I64Mapper(undefined));
        ObisCode obisCode = ObisCode.fromString("0.1.0.3.4.2");
        Mockito.when(offlineRegister.getObisCode()).thenReturn(obisCode);
        mapper.map(new BooleanObject(false), offlineRegister);
    }

    @Test
    public void dataType() {
        Assert.assertEquals(Array.class, new ArrayPositionMapper(ObisChannel.A, new U8Mapper(Unit.getUndefined())).dataType());
    }
}