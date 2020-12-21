package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BitStringMapperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private BitStringMapper mapper;

    @Before
    public void setUp() {
         mapper = new BitStringMapper();
    }

    @Test
    public void bitString() throws MappingException {
        BitString attribute = new BitString(0L);
        ObisCode obisCode = ObisCode.fromString("1.2.3.4.5.6");

        RegisterValue registerValue = mapper.map(attribute, obisCode);
        Assert.assertEquals(attribute.toString(), registerValue.getText());
        Assert.assertEquals(obisCode, registerValue.getObisCode());
    }

    @Test
    public void notBitString() throws MappingException {
        expectedException.expect(MappingException.class);
        mapper.map(new OctetString("a".getBytes()), ObisCode.fromString("1.2.3.4.5.6"));
    }

    @Test
    public void dataType() {
        Assert.assertEquals(BitString.class, mapper.dataType());
    }


}