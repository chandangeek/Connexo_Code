package com.elster.jupiter.pki.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.impl.wrappers.assymetric.PlaintextPrivateKeyFactory;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PKIServiceImplIT {

    private static PkiInMemoryPersistence pkiInMemoryPersistence = new PkiInMemoryPersistence();

    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();
    @Rule
    public TestRule transactionalRule = new TransactionalRule(pkiInMemoryPersistence.getTransactionService());

    @BeforeClass
    public static void initialize() {
        pkiInMemoryPersistence.activate();
    }

    @Before
    public void setUp() throws Exception {
        ((PkiServiceImpl)pkiInMemoryPersistence.getPkiService()).addPrivateKeyFactory(pkiInMemoryPersistence.getPlaintextPrivateKeyFactory());
    }

    @After
    public void tearDown() throws Exception {
        ((PkiServiceImpl)pkiInMemoryPersistence.getPkiService()).removePrivateKeyFactory(pkiInMemoryPersistence.getPlaintextPrivateKeyFactory());
    }

    @Test
    @Transactional
    public void testCreateSymmetricKey() {
        KeyType created = pkiInMemoryPersistence.getPkiService().newSymmetricKeyType("AES128", "AES", 128);
        Optional<KeyType> keyType = pkiInMemoryPersistence.getPkiService().getKeyType("AES128");
        assertThat(keyType).isPresent();
        assertThat(keyType.get().getName()).isEqualTo("AES128");
        assertThat(keyType.get().getAlgorithm()).isEqualTo("AES");
        assertThat(keyType.get().getCryptographicType()).isEqualTo(CryptographicType.SymmetricKey);
        assertThat(keyType.get().getKeySize()).isEqualTo(128);
        assertThat(keyType.get().getCurve()).isNull();
    }

    @Test
    @Transactional
    public void testCreateRSAKey() {
        KeyType created = pkiInMemoryPersistence.getPkiService().newAsymmetricKeyType("RSA2048").RSA().keySize(2048).add();
        Optional<KeyType> keyType = pkiInMemoryPersistence.getPkiService().getKeyType("RSA2048");
        assertThat(keyType).isPresent();
        assertThat(keyType.get().getName()).isEqualTo("RSA2048");
        assertThat(keyType.get().getAlgorithm()).isEqualTo("RSA");
        assertThat(keyType.get().getCryptographicType()).isEqualTo(CryptographicType.AsymmetricKey);
        assertThat(keyType.get().getKeySize()).isEqualTo(2048);
        assertThat(keyType.get().getCurve()).isNull();
    }

    @Test
    @Transactional
    public void testCreateDSAKey() {
        KeyType created = pkiInMemoryPersistence.getPkiService().newAsymmetricKeyType("DSA1024").DSA().keySize(1024).add();
        Optional<KeyType> keyType = pkiInMemoryPersistence.getPkiService().getKeyType("DSA1024");
        assertThat(keyType).isPresent();
        assertThat(keyType.get().getName()).isEqualTo("DSA1024");
        assertThat(keyType.get().getAlgorithm()).isEqualTo("DSA");
        assertThat(keyType.get().getCryptographicType()).isEqualTo(CryptographicType.AsymmetricKey);
        assertThat(keyType.get().getKeySize()).isEqualTo(1024);
        assertThat(keyType.get().getCurve()).isNull();
    }

    @Test
    @Transactional
    public void testCreateECKey() {
        KeyType created = pkiInMemoryPersistence.getPkiService().newAsymmetricKeyType("NIST P-256").ECDSA().curve("secp256r1").add();
        Optional<KeyType> keyType = pkiInMemoryPersistence.getPkiService().getKeyType("NIST P-256");
        assertThat(keyType).isPresent();
        assertThat(keyType.get().getName()).isEqualTo("NIST P-256");
        assertThat(keyType.get().getAlgorithm()).isEqualTo("ECDSA");
        assertThat(keyType.get().getCryptographicType()).isEqualTo(CryptographicType.AsymmetricKey);
        assertThat(keyType.get().getKeySize()).isEqualTo(0); // CXO-5375, I expect null here
        assertThat(keyType.get().getCurve()).isEqualTo("secp256r1");
    }

    @Test
    @Transactional
    public void testCreateECPlaintextPrivateKey() throws
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException, NoSuchProviderException {
        KeyType keyType = pkiInMemoryPersistence.getPkiService().newAsymmetricKeyType("NIST P-256K").ECDSA().curve("secp256k1").add();

        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getKeyType()).thenReturn(keyType);
        when(keyAccessorType.getKeyEncryptionMethod()).thenReturn(PlaintextPrivateKeyFactory.KEY_ENCRYPTION_METHOD);
        PrivateKeyWrapper privateKeyWrapper = pkiInMemoryPersistence.getPkiService().newPrivateKeyWrapper(keyAccessorType);
        privateKeyWrapper.renewValue();

        assertThat(privateKeyWrapper.getPrivateKey().getEncoded()).isNotEmpty();
        assertThat(privateKeyWrapper.getPrivateKey().getAlgorithm()).isEqualTo("ECDSA");
        assertThat(privateKeyWrapper.getPrivateKey().getFormat()).isEqualTo("PKCS#8");
        assertThat(privateKeyWrapper.getProperties()).hasSize(1);
        assertThat(privateKeyWrapper.getProperties()).containsKey("privateKey");
        assertThat(privateKeyWrapper.getPropertySpecs()).hasSize(1);
        assertThat(privateKeyWrapper.getPropertySpecs().get(0).getDisplayName()).isEqualTo("Private key");
        assertThat(privateKeyWrapper.getPropertySpecs().get(0).getDescription()).isEqualTo("Plaintext view of private key");
        assertThat(privateKeyWrapper.getPropertySpecs().get(0).getValueFactory().getValueType()).isEqualTo(String.class);
    }

    @Test
    @Transactional
    public void testCreateRSAPlaintextPrivateKey() throws
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException, NoSuchProviderException {
        KeyType keyType = pkiInMemoryPersistence.getPkiService().newAsymmetricKeyType("Some RSA key").RSA().keySize(2048).add();

        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getKeyType()).thenReturn(keyType);
        when(keyAccessorType.getKeyEncryptionMethod()).thenReturn(PlaintextPrivateKeyFactory.KEY_ENCRYPTION_METHOD);
        PrivateKeyWrapper privateKeyWrapper = pkiInMemoryPersistence.getPkiService().newPrivateKeyWrapper(keyAccessorType);
        privateKeyWrapper.renewValue();

        assertThat(privateKeyWrapper.getPrivateKey().getEncoded()).isNotEmpty();
        assertThat(privateKeyWrapper.getPrivateKey().getAlgorithm()).isEqualTo("RSA");
        assertThat(privateKeyWrapper.getPrivateKey().getFormat()).isEqualTo("PKCS#8");
        assertThat(privateKeyWrapper.getProperties()).hasSize(1);
        assertThat(privateKeyWrapper.getProperties()).containsKey("privateKey");
        assertThat(privateKeyWrapper.getPropertySpecs()).hasSize(1);
        assertThat(privateKeyWrapper.getPropertySpecs().get(0).getDisplayName()).isEqualTo("Private key");
        assertThat(privateKeyWrapper.getPropertySpecs().get(0).getDescription()).isEqualTo("Plaintext view of private key");
        assertThat(privateKeyWrapper.getPropertySpecs().get(0).getValueFactory().getValueType()).isEqualTo(String.class);
    }

    @Test
    @Transactional
    public void testCreateDSAPlaintextPrivateKey() throws
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException, NoSuchProviderException {
        KeyType keyType = pkiInMemoryPersistence.getPkiService().newAsymmetricKeyType("Some DSA key").DSA().keySize(512).add();

        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getKeyType()).thenReturn(keyType);
        when(keyAccessorType.getKeyEncryptionMethod()).thenReturn(PlaintextPrivateKeyFactory.KEY_ENCRYPTION_METHOD);
        PrivateKeyWrapper privateKeyWrapper = pkiInMemoryPersistence.getPkiService().newPrivateKeyWrapper(keyAccessorType);
        privateKeyWrapper.renewValue();

        assertThat(privateKeyWrapper.getPrivateKey().getEncoded()).isNotEmpty();
        assertThat(privateKeyWrapper.getPrivateKey().getAlgorithm()).isEqualTo("DSA");
        assertThat(privateKeyWrapper.getPrivateKey().getFormat()).isEqualTo("PKCS#8");
        assertThat(privateKeyWrapper.getProperties()).hasSize(1);
        assertThat(privateKeyWrapper.getProperties()).containsKey("privateKey");
        assertThat(privateKeyWrapper.getPropertySpecs()).hasSize(1);
        assertThat(privateKeyWrapper.getPropertySpecs().get(0).getDisplayName()).isEqualTo("Private key");
        assertThat(privateKeyWrapper.getPropertySpecs().get(0).getDescription()).isEqualTo("Plaintext view of private key");
        assertThat(privateKeyWrapper.getPropertySpecs().get(0).getValueFactory().getValueType()).isEqualTo(String.class);
    }


}
