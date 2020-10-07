/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.hsm.model.FUAKPassiveGenerationNotSupportedException;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.HsmNotConfiguredException;
import com.elster.jupiter.hsm.model.keys.HsmJssKeyType;
import com.elster.jupiter.hsm.model.keys.HsmKeyType;
import com.elster.jupiter.hsm.model.keys.HsmRenewKey;
import com.elster.jupiter.hsm.model.keys.SessionKeyCapability;
import com.elster.jupiter.hsm.model.request.RenewKeyRequest;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.HsmKey;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityAccessorType;
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
import java.util.Optional;

import org.junit.Assert;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HsmKeyTest {

    private static final String LABEL = "label";
    private static final byte[] KEY = "1234".getBytes();

    private HsmKeyImpl hsmKeyUnderTest;
    private HsmReversibleKey hsmReversibleKeyUnderTest;
    private Clock clock = Clock.system(ZoneId.systemDefault());

    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private KeyType keyType;
    @Mock
    private KeyType passwordType;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator validator;
    @Mock
    private HsmEnergyService hsmEnergyService;
    @Mock
    private HsmEncryptionService hsmEncryptionService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @Before
    public void setUp(){

        when(keyType.getKeyAlgorithm()).thenReturn("AES128");

        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(validator.validate(anyObject(), any(Class.class))).thenReturn(new HashSet<>());

        this.hsmKeyUnderTest = new HsmKeyImpl(propertySpecService, dataModel, clock, thesaurus, hsmEnergyService);
        this.hsmKeyUnderTest.init(keyType, new TimeDuration(1, TimeDuration.TimeUnit.DAYS), LABEL, HsmJssKeyType.AES);
        this.hsmReversibleKeyUnderTest = new HsmReversibleKey(propertySpecService, dataModel, clock, thesaurus, hsmEnergyService, hsmEncryptionService);
        this.hsmReversibleKeyUnderTest.init(passwordType, new TimeDuration(1, TimeDuration.TimeUnit.DAYS), LABEL, HsmJssKeyType.AUTHENTICATION);
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
        assertNull(hsmKeyUnderTest.getKey());
    }

    @Test
    public void getKeyReturnsInitialByteArray(){
        String encryptedKey = "ENC-KEY";
        hsmKeyUnderTest.setKey(KEY, LABEL);
        assertTrue(Arrays.equals(KEY, hsmKeyUnderTest.getKey()));
        assertEquals(LABEL, hsmKeyUnderTest.getLabel());
        verify(dataModel).persist(this.hsmKeyUnderTest);
    }

    @Test
    public void generateNewPasswordOrHLSecret() throws HsmBaseException, HsmNotConfiguredException {
        // This is an awful test yet this is the model we have and need to mock a bunch of stuff ... my apologies :)
        SecurityAccessorType securityAccesorType = mock(SecurityAccessorType.class);
        HsmKeyType keyType = new HsmKeyType(HsmJssKeyType.AUTHENTICATION,"passwordLabel", SessionKeyCapability.DC_KEK_NONAUTHENTIC, SessionKeyCapability.DC_KEK_RENEWAL, 16, false);
        when(securityAccesorType.getHsmKeyType()).thenReturn(keyType);

        HsmReversibleKey currentKey = mock(HsmReversibleKey.class);
        byte[] cKey = "ckey".getBytes();
        when(currentKey.getKey()).thenReturn(cKey);

        when(hsmEncryptionService.symmetricEncrypt(anyObject(), any(String.class))).thenReturn("encrypted".getBytes());
        when(hsmEncryptionService.symmetricDecrypt(anyObject(), any(String.class))).thenReturn("plaintext".getBytes());

        hsmReversibleKeyUnderTest.generateValue(securityAccesorType, null);
        Assert.assertArrayEquals("plaintext".getBytes(), hsmReversibleKeyUnderTest.getKey());
        Assert.assertEquals(LABEL, hsmReversibleKeyUnderTest.getLabel());
    }


    @Test
    public void generateNewAESkey() throws HsmBaseException, FUAKPassiveGenerationNotSupportedException {
        // This is an awful test yet this is the model we have and need to mock a bunch of stuff ... my apologies :)
        SecurityAccessorType securityAccesorType = mock(SecurityAccessorType.class);
        HsmKeyType keyType = new HsmKeyType(HsmJssKeyType.AES,"label", SessionKeyCapability.DC_KEK_NONAUTHENTIC, SessionKeyCapability.DC_KEK_RENEWAL, 16, false);
        when(securityAccesorType.getHsmKeyType()).thenReturn(keyType);


        HsmKey currentKey = mock(HsmKey.class);
        String clabel = "clabel";
        when(currentKey.getLabel()).thenReturn(clabel);
        byte[] cKey = "ckey".getBytes();
        when(currentKey.getKey()).thenReturn(cKey);

        HsmRenewKey hsmRenewKey = new HsmRenewKey("smKey".getBytes(), "rKey".getBytes(), "rlabel");
        when(hsmEnergyService.renewKey(new RenewKeyRequest(anyObject(),clabel, keyType))).thenReturn(hsmRenewKey);

        hsmKeyUnderTest.generateValue(securityAccesorType, Optional.of(currentKey));
        // new label comes from hsm key type
        Assert.assertEquals(hsmRenewKey.getLabel(), hsmKeyUnderTest.getLabel());
        Assert.assertArrayEquals(hsmRenewKey.getKey(), hsmKeyUnderTest.getKey());
        Assert.assertArrayEquals(hsmRenewKey.getSmartMeterKey(), hsmKeyUnderTest.getSmartMeterKey());
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
        hsmKeyUnderTest.setKey(KEY, LABEL);
        Map<String, Object> properties = hsmKeyUnderTest.getProperties();

        assertEquals(DatatypeConverter.printHexBinary(KEY), properties.get(HsmProperties.DECRYPTED_KEY.getPropertyName()));
        assertEquals(LABEL, properties.get(HsmProperties.LABEL.getPropertyName()));
    }


}
