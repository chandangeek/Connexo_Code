package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.axrdencoding.Integer64;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class I64MapperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private OfflineRegister offlineRegister;

    private I64Mapper mapper;
    private final Unit unit = Unit.get("Pa");

    @Before
    public void setUp() {
        mapper = new I64Mapper(unit);
    }

    @Test
    public void i64Type() throws MappingException {
        Integer64 attribute = new Integer64(123);
        ObisCode obisCode = ObisCode.fromString("1.2.3.4.5.6");
        Mockito.when(offlineRegister.getObisCode()).thenReturn(obisCode);
        RegisterValue registerValue = mapper.map(attribute, offlineRegister);
        Assert.assertEquals(attribute.toBigDecimal(), registerValue.getQuantity().getAmount());
        Assert.assertEquals(unit, registerValue.getQuantity().getUnit());
        Assert.assertEquals(obisCode, registerValue.getObisCode());
    }

    @Test
    public void notI64Type() throws MappingException {
        expectedException.expect(MappingException.class);
        mapper.map(new OctetString("a".getBytes()), offlineRegister);
    }

    @Test
    public void dataType() {
        Assert.assertEquals(Integer64.class, mapper.dataType());
    }


}