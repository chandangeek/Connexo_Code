package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DefaultMapperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private DefaultMapper mapper;

    @Before
    public void setUp() {
        mapper = new DefaultMapper();
    }

    @Test
    public void mappableToString()  {
        BooleanObject attribute = new BooleanObject(true);
        ObisCode obisCode = ObisCode.fromString("1.2.3.4.5.6");
        RegisterValue registerValue = mapper.map(attribute, obisCode);
        Assert.assertEquals(attribute.toString(), registerValue.getText());
        Assert.assertEquals(obisCode, registerValue.getObisCode());
    }

    @Test
    public void dataType() {
        Assert.assertEquals(AbstractDataType.class, mapper.dataType());
    }
}