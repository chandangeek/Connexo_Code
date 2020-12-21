package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;


import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.common.obis.readers.MappingException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ArrayMapperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ArrayMapper mapper;

    @Before
    public void setUp() {
        mapper = new ArrayMapper();
    }

    @Test
    public void array() throws MappingException {
        Array array = new Array();
        ObisCode obisCode = ObisCode.fromString("1.2.3.4.5.6");
        RegisterValue registerValue = mapper.map(array, obisCode);
        Assert.assertEquals(array.toString(), registerValue.getText());
        Assert.assertEquals(obisCode, registerValue.getObisCode());
    }

    @Test
    public void notArray() throws MappingException {
        expectedException.expect(MappingException.class);
        mapper.map(new BitString(0L), ObisCode.fromString("1.2.3.4.5.6"));
    }

    @Test
    public void dataType() {
        Assert.assertEquals(Array.class, mapper.dataType());
    }

}