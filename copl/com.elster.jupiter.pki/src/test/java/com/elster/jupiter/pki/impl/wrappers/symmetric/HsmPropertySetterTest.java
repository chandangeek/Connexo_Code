/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Ignore("Requirements are not defined for the property setter. So nothing to test")
public class HsmPropertySetterTest {

    private static final byte[] KEY = "1234".getBytes();
    private static final String LABEL = "label";

    private HsmPropertySetter propertySetter;

    @Mock
    private HsmKeyImpl symmetricKey;

    @Before
    public void setUp(){
        when(symmetricKey.getKey()).thenReturn(KEY);
        when(symmetricKey.getLabel()).thenReturn(LABEL);
        this.propertySetter = new HsmPropertySetter(symmetricKey);
    }

    @Test
    public void getKeyAsHexBinary() {
        assertEquals(this.propertySetter.getHexBinaryKey(), DatatypeConverter.printHexBinary(KEY));
    }

    @Test
    public void getKeyAsByteArray() {
        assertTrue(Arrays.equals(KEY, this.propertySetter.getKey()));
    }

    @Test
    public void emptyKey(){
        when(symmetricKey.getKey()).thenReturn(new byte[0]);
        this.propertySetter = new HsmPropertySetter(symmetricKey);

        assertEquals("", this.propertySetter.getHexBinaryKey());
        assertTrue(Arrays.equals(new byte[0], this.propertySetter.getKey()));
    }

    @Test
    public void emptyLabel(){
        when(symmetricKey.getLabel()).thenReturn("");
        this.propertySetter = new HsmPropertySetter(symmetricKey);

        assertEquals("",propertySetter.getLabel());
    }
}
