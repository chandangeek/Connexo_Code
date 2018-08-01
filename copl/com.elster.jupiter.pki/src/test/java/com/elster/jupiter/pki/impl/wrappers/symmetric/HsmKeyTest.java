/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.HsmEncryptedKey;
import com.elster.jupiter.hsm.model.request.RenewKeyRequest;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.HsmKey;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.TimeDuration;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.xml.bind.DatatypeConverter;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HsmKeyTest {

    private static final String LABEL = "label";
    private static final byte[] KEY = "1234".getBytes();

    private HsmKeyImpl hsmKeyUnderTest;
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

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @Before
    public void setUp(){

        when(keyType.getKeyAlgorithm()).thenReturn("AES128");

        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(validator.validate(anyObject(), any(Class.class))).thenReturn(new HashSet<>());

        this.hsmKeyUnderTest = new HsmKeyImpl(dataVaultService, propertySpecService, dataModel, clock, thesaurus, hsmEnergyService);
        this.hsmKeyUnderTest.init(keyType, new TimeDuration(1, TimeDuration.TimeUnit.DAYS), LABEL);

    }


    @Test(expected = IllegalArgumentException.class)
    public void setKeyWithNullKey(){
        this.hsmKeyUnderTest.setKey(null, LABEL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setKeyWithNulLabel(){
        this.hsmKeyUnderTest.setKey(KEY, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setKeyWithEmptyabel(){
        this.hsmKeyUnderTest.setKey(KEY, "");
    }

    @Test
    public void getKeyWhenNotSet(){
        reset(dataVaultService);
        when(dataVaultService.decrypt(null)).thenReturn(null);
        assertNull(hsmKeyUnderTest.getKey());
    }

    @Test
    public void getKeyReturnsInitialByteArray(){
        String encryptedKey = "ENC-KEY";
        when(dataVaultService.encrypt(KEY)).thenReturn(encryptedKey);
        when(dataVaultService.decrypt(encryptedKey)).thenReturn(KEY);
        hsmKeyUnderTest.setKey(KEY, LABEL);
        assertTrue(Arrays.equals(KEY, hsmKeyUnderTest.getKey()));
        assertEquals(LABEL, hsmKeyUnderTest.getLabel());
        verify(dataModel).persist(this.hsmKeyUnderTest);
    }

    @Test
    public void generateValue() throws HsmBaseException {
        // mocking request
        HsmKey mockedCurrentKey = mock(HsmKey.class);
        HsmEncryptedKey hsmEncryptedKey = mockHsmEnergyService(mockedCurrentKey);

        String ecnryptedResultKey = "abcdef";
        when(dataVaultService.encrypt(hsmEncryptedKey.getEncryptedKey())).thenReturn(ecnryptedResultKey);
        when(dataVaultService.decrypt(ecnryptedResultKey)).thenReturn(ecnryptedResultKey.getBytes());

        hsmKeyUnderTest.generateValue(mockedCurrentKey);

        assertTrue(Arrays.equals(ecnryptedResultKey.getBytes(), hsmKeyUnderTest.getKey()));
        assertEquals(LABEL, hsmKeyUnderTest.getLabel());

        verify(dataModel).persist(this.hsmKeyUnderTest);
    }


    @Test
    public void setPropertiesWithMissingKey(){
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Key cannot be null");
        Map<String, Object> props = new HashMap<>();
        hsmKeyUnderTest.setProperties(props);
    }

    @Test
    public void setPropertiesWithLabel(){
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Label cannot be null");
        Map<String, Object> props = new HashMap<>();
        props.put(HsmProperties.DECRYPTED_KEY.getPropertyName(), "ABCD");
        hsmKeyUnderTest.setProperties(props);
    }

    @Test
    public void setPropertiesWithNonHexValue(){
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("contains illegal character for hexBinary: XXXX");
        Map<String, Object> props = new HashMap<>();
        props.put(HsmProperties.DECRYPTED_KEY.getPropertyName(), "XXXX");
        hsmKeyUnderTest.setProperties(props);
    }

    @Test
    public void setProperties(){
        String encKey = "encKey";
        when(dataVaultService.encrypt(KEY)).thenReturn(encKey);
        when(dataVaultService.decrypt(encKey)).thenReturn(KEY);
        Map<String, Object> props = new HashMap<>();
        props.put(HsmProperties.DECRYPTED_KEY.getPropertyName(), DatatypeConverter.printHexBinary(KEY));
        String modifiedLabel = "modifiedLabel";
        props.put(HsmProperties.LABEL.getPropertyName(), modifiedLabel);
        hsmKeyUnderTest.setProperties(props);
        assertTrue(Arrays.equals(KEY, hsmKeyUnderTest.getKey()));
        assertEquals(modifiedLabel, hsmKeyUnderTest.getLabel());
    }

    @Test
    public void testGetPropertiesWithoutKey(){
        Map<String, Object> properties = hsmKeyUnderTest.getProperties();

        assertTrue(properties.size() == 1);
        assertEquals(LABEL, properties.get(HsmProperties.LABEL.getPropertyName()));
    }

    @Test
    public void testGetProperties(){
        String encKey = "encKey";
        when(dataVaultService.encrypt(KEY)).thenReturn(encKey);
        when(dataVaultService.decrypt(encKey)).thenReturn(KEY);
        hsmKeyUnderTest.setKey(KEY, LABEL);
        Map<String, Object> properties = hsmKeyUnderTest.getProperties();

        assertEquals(DatatypeConverter.printHexBinary(KEY), properties.get(HsmProperties.DECRYPTED_KEY.getPropertyName()));
        assertEquals(LABEL, properties.get(HsmProperties.LABEL.getPropertyName()));
    }

    private HsmEncryptedKey mockHsmEnergyService(HsmKey mockedCurrentKey) throws HsmBaseException {
        byte[] cKeyBytes = "cKey".getBytes();
        when(mockedCurrentKey.getKey()).thenReturn(cKeyBytes);
        String cLabel = "cLabel";
        when(mockedCurrentKey.getLabel()).thenReturn(cLabel);
        RenewKeyRequest renewKeyRequest = new RenewKeyRequest(cKeyBytes, cLabel, LABEL);

        // mocking call to used services:
        // energy service
        HsmEncryptedKey hsmEncryptedKey = new HsmEncryptedKey("hsmKey".getBytes(), LABEL);
        when(hsmEnergyService.renewKey(renewKeyRequest)).thenReturn(hsmEncryptedKey);
        return hsmEncryptedKey;
    }


}
