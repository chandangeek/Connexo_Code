package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class I32MapperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private I32Mapper mapper;
    private final Unit unit = Unit.get("Pa");

    @Before
    public void setUp() {
        mapper = new I32Mapper(unit);
    }

    @Test
    public void i32Type() throws MappingException {
        Integer32 attribute = new Integer32(123);
        ObisCode obisCode = ObisCode.fromString("1.2.3.4.5.6");
        RegisterValue registerValue = mapper.map(attribute, obisCode);
        Assert.assertEquals(attribute.toBigDecimal(), registerValue.getQuantity().getAmount());
        Assert.assertEquals(unit, registerValue.getQuantity().getUnit());
        Assert.assertEquals(obisCode, registerValue.getObisCode());
    }

    @Test
    public void notI32Type() throws MappingException {
        expectedException.expect(MappingException.class);
        mapper.map(new OctetString("a".getBytes()), ObisCode.fromString("1.2.3.4.5.6"));
    }

    @Test
    public void dataType() {
        Assert.assertEquals(Integer32.class, mapper.dataType());
    }


}