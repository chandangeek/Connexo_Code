package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.mapper;


import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BitString;
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
public class ArrayMapperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private OfflineRegister offlineRegister;

    private ArrayMapper mapper;

    @Before
    public void setUp() {
        mapper = new ArrayMapper();
    }

    @Test
    public void array() throws MappingException {
        Array array = new Array();
        ObisCode obisCode = ObisCode.fromString("1.2.3.4.5.6");
        Mockito.when(offlineRegister.getObisCode()).thenReturn(obisCode);
        RegisterValue registerValue = mapper.map(array, offlineRegister);
        Assert.assertEquals(array.toString(), registerValue.getText());
        Assert.assertEquals(obisCode, registerValue.getObisCode());
    }

    @Test
    public void notArray() throws MappingException {
        expectedException.expect(MappingException.class);
        ObisCode obisCode = ObisCode.fromString("1.2.3.4.5.6");
        Mockito.when(offlineRegister.getObisCode()).thenReturn(obisCode);
        mapper.map(new BitString(0L), offlineRegister);
    }

    @Test
    public void dataType() {
        Assert.assertEquals(Array.class, mapper.dataType());
    }

}