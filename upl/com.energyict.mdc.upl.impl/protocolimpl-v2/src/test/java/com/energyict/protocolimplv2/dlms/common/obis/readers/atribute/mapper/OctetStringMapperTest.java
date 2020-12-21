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

public class OctetStringMapperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private OctetStringMapper mapper;

    @Before
    public void setUp() {
         mapper = new OctetStringMapper();
    }

    @Test
    public void octetString() throws MappingException {
        OctetString attribute = new OctetString("abc".getBytes());
        ObisCode obisCode = ObisCode.fromString("1.2.3.4.5.6");

        RegisterValue registerValue = mapper.map(attribute, obisCode);
        Assert.assertEquals(attribute.stringValue(), registerValue.getText());
        Assert.assertEquals(obisCode, registerValue.getObisCode());
    }

    @Test
    public void notOctetString() throws MappingException {
        expectedException.expect(MappingException.class);
        mapper.map(new BitString(0L), ObisCode.fromString("1.2.3.4.5.6"));
    }

    @Test
    public void dataType() {
        Assert.assertEquals(OctetString.class, mapper.dataType());
    }


}