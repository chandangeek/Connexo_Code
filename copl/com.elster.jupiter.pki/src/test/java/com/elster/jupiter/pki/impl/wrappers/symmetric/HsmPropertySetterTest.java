/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.pki.HsmSymmetricKey;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.wrappers.HexBinaryValidator;

import javax.validation.ConstraintValidatorContext;
import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HsmPropertySetterTest {

    private static final byte[] KEY = "1234".getBytes();
    private static final String LABEL = "label";

    private HsmPropertySetter propertySetter;

    @Mock
    private HsmSymmetricKeyImpl symmetricKey;

    @Before
    public void setUp(){
        when(symmetricKey.getKey()).thenReturn(KEY);
        when(symmetricKey.getKeyLabel()).thenReturn(LABEL);
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
        when(symmetricKey.getKeyLabel()).thenReturn("");
        this.propertySetter = new HsmPropertySetter(symmetricKey);

        assertEquals("",propertySetter.getLabel());
    }
}
