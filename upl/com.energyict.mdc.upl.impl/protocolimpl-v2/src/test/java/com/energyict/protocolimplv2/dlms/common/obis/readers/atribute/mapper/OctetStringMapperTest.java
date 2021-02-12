package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;

import com.energyict.dlms.axrdencoding.BitString;
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
public class OctetStringMapperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private OfflineRegister offlineRegister;

    private OctetStringMapper mapper;

    @Before
    public void setUp() {
         mapper = new OctetStringMapper();
    }

    @Test
    public void octetString() throws MappingException {
        OctetString attribute = new OctetString("abc".getBytes());
        ObisCode obisCode = ObisCode.fromString("1.2.3.4.5.6");
        Mockito.when(offlineRegister.getObisCode()).thenReturn(obisCode);
        RegisterValue registerValue = mapper.map(attribute, offlineRegister);
        Assert.assertEquals(attribute.stringValue(), registerValue.getText());
        Assert.assertEquals(obisCode, registerValue.getObisCode());
    }

    @Test
    public void notOctetString() throws MappingException {
        expectedException.expect(MappingException.class);
        mapper.map(new BitString(0L), offlineRegister);
    }

    @Test
    public void dataType() {
        Assert.assertEquals(OctetString.class, mapper.dataType());
    }


}