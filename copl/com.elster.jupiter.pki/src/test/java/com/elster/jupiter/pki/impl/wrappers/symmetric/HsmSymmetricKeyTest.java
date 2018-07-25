/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.TimeDuration;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.xml.bind.DatatypeConverter;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HsmSymmetricKeyTest {

    private static final String LABEL = "label";
    private static final byte[] KEY = "1234".getBytes();

    private HsmKeyImpl hsmSymmetricKey;
    private Clock clock = Clock.system(ZoneId.systemDefault());

    @Mock
    private DataVaultService dataVaultService;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private KeyType keyType;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator validator;
    @Mock
    private HsmEnergyService hsmEnergyService;

    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @Before
    public void setUp(){

        when(keyType.getKeyAlgorithm()).thenReturn("AES128");
        when(dataVaultService.encrypt(any())).then(invocationOnMock -> Base64.getEncoder().encodeToString((byte[]) invocationOnMock.getArguments()[0]));
        when(dataVaultService.decrypt(any())).then(invocationOnMock -> Base64.getDecoder().decode((String) invocationOnMock.getArguments()[0]));
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(validator.validate(anyObject(), any(Class.class))).thenReturn(new HashSet<>());

        this.hsmSymmetricKey = new HsmKeyImpl(dataVaultService, propertySpecService, dataModel, clock, thesaurus, hsmEnergyService);
        this.hsmSymmetricKey.init(keyType, new TimeDuration(1, TimeDuration.TimeUnit.DAYS), LABEL);

    }


    @Test(expected = IllegalArgumentException.class)
    public void setNullKey(){
        this.hsmSymmetricKey.setKey(null, LABEL);
    }

    @Test
    public void whenKeyWasNotSet_ThenEmptyKey(){
        assertThat(hsmSymmetricKey.getKey()).isEmpty();
    }

    @Test
    public void persistedKeyIsBase64Encoded(){
        hsmSymmetricKey.setKey(KEY, LABEL);
        String encryptedKey = hsmSymmetricKey.getEncryptedKey();
        byte[] decoded = dataVaultService.decrypt(encryptedKey);

        assertTrue(Arrays.equals(KEY, decoded));
    }

    @Test
    public void keyIsNoBases64tEncoded(){
        hsmSymmetricKey.setKey(KEY, LABEL);

        assertTrue(Arrays.equals(KEY, hsmSymmetricKey.getKey()));
    }

    @Test
    public void keyAndLabelAreProperties(){
        hsmSymmetricKey.setKey(KEY, LABEL);

        assertTrue(hsmSymmetricKey.getProperties().containsKey("key"));
        assertTrue(hsmSymmetricKey.getProperties().containsKey("label"));
    }

    @Test
    public void keyPropertyIsHexBinaryFormat(){
        hsmSymmetricKey.setKey(KEY, LABEL);
        String expected = DatatypeConverter.printHexBinary(KEY);
        String value = (String) hsmSymmetricKey.getProperties().get("key");

        assertEquals(expected, value);
    }

    @Test
    public void nullKeyProperty(){
        String expected = DatatypeConverter.printHexBinary(new byte[0]);
        String value = (String) hsmSymmetricKey.getProperties().get("key");

        assertEquals(expected, value);
    }


}
