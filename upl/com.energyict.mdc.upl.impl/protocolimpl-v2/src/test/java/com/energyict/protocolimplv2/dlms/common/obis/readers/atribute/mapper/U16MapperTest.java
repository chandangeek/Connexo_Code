package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class U16MapperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private U16Mapper mapper;
    private final Unit unit = Unit.get("Pa");

    @Before
    public void setUp() {
        mapper = new U16Mapper(unit);
    }

    @Test
    public void u16Type() throws MappingException {
        Unsigned16 attribute = new Unsigned16(1);
        ObisCode obisCode = ObisCode.fromString("1.2.3.4.5.6");
        RegisterValue registerValue = mapper.map(attribute, obisCode);
        Assert.assertEquals(attribute.toBigDecimal(), registerValue.getQuantity().getAmount());
        Assert.assertEquals(unit, registerValue.getQuantity().getUnit());
        Assert.assertEquals(obisCode, registerValue.getObisCode());
    }

    @Test
    public void notU16Type() throws MappingException {
        expectedException.expect(MappingException.class);
        mapper.map(new OctetString("a".getBytes()), ObisCode.fromString("1.2.3.4.5.6"));
    }

    @Test
    public void dataType() {
        Assert.assertEquals(Unsigned16.class, mapper.dataType());
    }


}