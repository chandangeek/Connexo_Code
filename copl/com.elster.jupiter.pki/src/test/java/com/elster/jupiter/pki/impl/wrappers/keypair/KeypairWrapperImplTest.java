package com.elster.jupiter.pki.impl.wrappers.keypair;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.KeyType;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 9/21/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class KeypairWrapperImplTest {
    @Mock
    DataModel dataModel;
    @Mock
    ValidatorFactory validatorFactory;
    @Mock
    Validator validator;

    @Before
    public void setUp() throws Exception {
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(validator.validate(anyObject(), any(Class.class))).thenReturn(Collections.emptySet());
    }

    @Test
    public void listProviders() throws Exception {
        Arrays.stream(Security.getProviders()).map(Provider::getName).forEach(System.out::println);
    }

    @Test
    public void testStoreReadRSAKey() throws Exception {
        KeyType keyType = mock(KeyType.class);
        when(keyType.getKeyAlgorithm()).thenReturn("RSA");
        KeypairWrapperImpl keypairWrapper = new KeypairWrapperImpl(dataModel, null, null, null).init(keyType, null);
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "SunJSSE");
        keyPairGenerator.initialize(1024, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        keypairWrapper.setPublicKey(keyPair.getPublic());
        assertThat(keypairWrapper.getPublicKey()).isPresent();
        assertThat(keypairWrapper.getPublicKey().get().getEncoded()).isEqualTo(keyPair.getPublic().getEncoded());
    }

    @Test
    public void testStoreRead_BC_RSAKey() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        KeyType keyType = mock(KeyType.class);
        when(keyType.getKeyAlgorithm()).thenReturn("RSA");
        KeypairWrapperImpl keypairWrapper = new KeypairWrapperImpl(dataModel, null, null, null).init(keyType, null);
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(1024, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        keypairWrapper.setPublicKey(keyPair.getPublic());

        assertThat(keypairWrapper.getPublicKey()).isPresent();
        assertThat(keypairWrapper.getPublicKey().get().getEncoded()).isEqualTo(keyPair.getPublic().getEncoded());
    }

    @Test
    public void testStoreRead_DSAKey() throws Exception {
        KeyType keyType = mock(KeyType.class);
        when(keyType.getKeyAlgorithm()).thenReturn("DSA");
        KeypairWrapperImpl keypairWrapper = new KeypairWrapperImpl(dataModel, null, null, null).init(keyType, null);
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA", "SUN");
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        keypairWrapper.setPublicKey(keyPair.getPublic());

        assertThat(keypairWrapper.getPublicKey()).isPresent();
        assertThat(keypairWrapper.getPublicKey().get().getEncoded()).isEqualTo(keyPair.getPublic().getEncoded());
    }

    @Test
    public void testStoreRead_BC_DSAKey() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        KeyType keyType = mock(KeyType.class);
        when(keyType.getKeyAlgorithm()).thenReturn("DSA");
        KeypairWrapperImpl keypairWrapper = new KeypairWrapperImpl(dataModel, null, null, null).init(keyType, null);
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA", "BC");
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        keypairWrapper.setPublicKey(keyPair.getPublic());
        System.out.printf("key="+ Hex.toHexString(keyPair.getPublic().getEncoded()));

        assertThat(keypairWrapper.getPublicKey()).isPresent();
        assertThat(keypairWrapper.getPublicKey().get().getEncoded()).isEqualTo(keyPair.getPublic().getEncoded());
    }

    @Test
    public void testStoreRead_ECKey() throws Exception {
        KeyType keyType = mock(KeyType.class);
        when(keyType.getKeyAlgorithm()).thenReturn("EC");
        KeypairWrapperImpl keypairWrapper = new KeypairWrapperImpl(dataModel, null, null, null).init(keyType, null);
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "SunEC");
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        keypairWrapper.setPublicKey(keyPair.getPublic());

        assertThat(keypairWrapper.getPublicKey()).isPresent();
        assertThat(keypairWrapper.getPublicKey().get().getEncoded()).isEqualTo(keyPair.getPublic().getEncoded());
    }

    @Test
    public void testStoreRead_BC_ECKey() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        KeyType keyType = mock(KeyType.class);
        when(keyType.getKeyAlgorithm()).thenReturn("EC");
        KeypairWrapperImpl keypairWrapper = new KeypairWrapperImpl(dataModel, null, null, null).init(keyType, null);
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        keypairWrapper.setPublicKey(keyPair.getPublic());

        assertThat(keypairWrapper.getPublicKey()).isPresent();
        assertThat(keypairWrapper.getPublicKey().get().getEncoded()).isEqualTo(keyPair.getPublic().getEncoded());
    }


}
