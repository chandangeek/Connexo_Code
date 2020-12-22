package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class U32MapperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private U32Mapper mapper;
    private final Unit unit = Unit.get("Pa");

    @Before
    public void setUp() {
        mapper = new U32Mapper(unit);
    }

    @Test
    public void u32Type() throws MappingException {
        Unsigned32 attribute = new Unsigned32(1);
        ObisCode obisCode = ObisCode.fromString("1.2.3.4.5.6");
        RegisterValue registerValue = mapper.map(attribute, obisCode);
        Assert.assertEquals(attribute.toBigDecimal(), registerValue.getQuantity().getAmount());
        Assert.assertEquals(unit, registerValue.getQuantity().getUnit());
        Assert.assertEquals(obisCode, registerValue.getObisCode());
    }

    @Test
    public void notU32Type() throws MappingException {
        expectedException.expect(MappingException.class);
        mapper.map(new OctetString("a".getBytes()), ObisCode.fromString("1.2.3.4.5.6"));
    }

    @Test
    public void dataType() {
        Assert.assertEquals(Unsigned32.class, mapper.dataType());
    }


}