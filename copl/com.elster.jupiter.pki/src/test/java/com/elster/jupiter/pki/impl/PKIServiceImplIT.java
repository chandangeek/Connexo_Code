package com.elster.jupiter.pki.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.pki.impl.wrappers.assymetric.DataVaultPrivateKeyFactory;
import com.elster.jupiter.pki.impl.wrappers.symmetric.DataVaultSymmetricKeyFactory;
import com.elster.jupiter.pki.impl.wrappers.symmetric.PlaintextSymmetricKey;

import certpathvalidator.CertPathValidatorTest;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
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

    private static PkiInMemoryPersistence inMemoryPersistence = new PkiInMemoryPersistence();

    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();
    @Rule
    public TestRule transactionalRule = new TransactionalRule(inMemoryPersistence.getTransactionService());
    private CertificateFactory certificateFactory;

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence.activate();
    }

    @Before
    public void setUp() throws Exception {
        ((PkiServiceImpl) inMemoryPersistence.getPkiService()).addPrivateKeyFactory(inMemoryPersistence.getDataVaultPrivateKeyFactory());
        ((PkiServiceImpl) inMemoryPersistence.getPkiService()).addSymmetricKeyFactory(inMemoryPersistence.getDataVaultSymmetricKeyFactory());
        Security.addProvider(new BouncyCastleProvider());
        certificateFactory = CertificateFactory.getInstance("X.509", "BC");
    }

    @After
    public void tearDown() throws Exception {
        ((PkiServiceImpl) inMemoryPersistence.getPkiService()).removePrivateKeyFactory(inMemoryPersistence.getDataVaultPrivateKeyFactory());
        ((PkiServiceImpl) inMemoryPersistence.getPkiService()).removeSymmetricKeyFactory(inMemoryPersistence.getDataVaultSymmetricKeyFactory());
    }

    @Test
    @Transactional
    public void testCreateSymmetricKey() {
        KeyType created = inMemoryPersistence.getPkiService()
                .newSymmetricKeyType("AES128", "AES", 128)
                .description("hello")
                .add();
        Optional<KeyType> keyType = inMemoryPersistence.getPkiService().getKeyType("AES128");
        assertThat(keyType).isPresent();
        assertThat(keyType.get().getName()).isEqualTo("AES128");
        assertThat(keyType.get().getAlgorithm()).isEqualTo("AES");
        assertThat(keyType.get().getCryptographicType()).isEqualTo(CryptographicType.SymmetricKey);
        assertThat(keyType.get().getKeySize()).isEqualTo(128);
        assertThat(keyType.get().getDescription()).isEqualTo("hello");
        assertThat(keyType.get().getCurve()).isNull();
    }

    @Test
    @Transactional
    public void testCreateRSAKey() {
        KeyType created = inMemoryPersistence.getPkiService()
                .newAsymmetricKeyType("RSA2048")
                .description("boe")
                .RSA()
                .keySize(2048)
                .add();
        Optional<KeyType> keyType = inMemoryPersistence.getPkiService().getKeyType("RSA2048");
        assertThat(keyType).isPresent();
        assertThat(keyType.get().getName()).isEqualTo("RSA2048");
        assertThat(keyType.get().getAlgorithm()).isEqualTo("RSA");
        assertThat(keyType.get().getDescription()).isEqualTo("boe");
        assertThat(keyType.get().getCryptographicType()).isEqualTo(CryptographicType.AsymmetricKey);
        assertThat(keyType.get().getKeySize()).isEqualTo(2048);
        assertThat(keyType.get().getCurve()).isNull();
    }

    @Test
    @Transactional
    public void testCreateDSAKey() {
        KeyType created = inMemoryPersistence.getPkiService().newAsymmetricKeyType("DSA1024").DSA().keySize(1024).add();
        Optional<KeyType> keyType = inMemoryPersistence.getPkiService().getKeyType("DSA1024");
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
        KeyType created = inMemoryPersistence.getPkiService()
                .newAsymmetricKeyType("NIST P-256")
                .description("check")
                .ECDSA()
                .curve("secp256r1")
                .add();
        Optional<KeyType> keyType = inMemoryPersistence.getPkiService().getKeyType("NIST P-256");
        assertThat(keyType).isPresent();
        assertThat(keyType.get().getName()).isEqualTo("NIST P-256");
        assertThat(keyType.get().getAlgorithm()).isEqualTo("ECDSA");
        assertThat(keyType.get().getDescription()).isEqualTo("check");
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
        KeyType keyType = inMemoryPersistence.getPkiService()
                .newAsymmetricKeyType("NIST P-256K")
                .ECDSA()
                .curve("secp256k1")
                .add();

        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getKeyType()).thenReturn(keyType);
        when(keyAccessorType.getKeyEncryptionMethod()).thenReturn(DataVaultPrivateKeyFactory.KEY_ENCRYPTION_METHOD);
        PrivateKeyWrapper privateKeyWrapper = inMemoryPersistence.getPkiService().newPrivateKeyWrapper(keyAccessorType);
        privateKeyWrapper.renewValue();

        assertThat(privateKeyWrapper.getPrivateKey().getEncoded()).isNotEmpty();
        assertThat(privateKeyWrapper.getPrivateKey().getAlgorithm()).isEqualTo("ECDSA");
        assertThat(privateKeyWrapper.getPrivateKey().getFormat()).isEqualTo("PKCS#8");
        assertThat(privateKeyWrapper.getProperties()).hasSize(1);
        assertThat(privateKeyWrapper.getProperties()).containsKey("privateKey");
        assertThat(privateKeyWrapper.getPropertySpecs()).hasSize(1);
        assertThat(privateKeyWrapper.getPropertySpecs().get(0).getDisplayName()).isEqualTo("Private key");
        assertThat(privateKeyWrapper.getPropertySpecs()
                .get(0)
                .getDescription()).isEqualTo("Plaintext view of private key");
        assertThat(privateKeyWrapper.getPropertySpecs()
                .get(0)
                .getValueFactory()
                .getValueType()).isEqualTo(String.class);
    }

    @Test
    @Transactional
    public void testCreateRSAPlaintextPrivateKey() throws
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException, NoSuchProviderException {
        KeyType keyType = inMemoryPersistence.getPkiService()
                .newAsymmetricKeyType("Some RSA key")
                .RSA()
                .keySize(2048)
                .add();

        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getKeyType()).thenReturn(keyType);
        when(keyAccessorType.getKeyEncryptionMethod()).thenReturn(DataVaultPrivateKeyFactory.KEY_ENCRYPTION_METHOD);
        PrivateKeyWrapper privateKeyWrapper = inMemoryPersistence.getPkiService().newPrivateKeyWrapper(keyAccessorType);
        privateKeyWrapper.renewValue();

        assertThat(privateKeyWrapper.getPrivateKey().getEncoded()).isNotEmpty();
        assertThat(privateKeyWrapper.getPrivateKey().getAlgorithm()).isEqualTo("RSA");
        assertThat(privateKeyWrapper.getPrivateKey().getFormat()).isEqualTo("PKCS#8");
        assertThat(privateKeyWrapper.getProperties()).hasSize(1);
        assertThat(privateKeyWrapper.getProperties()).containsKey("privateKey");
        assertThat(privateKeyWrapper.getPropertySpecs()).hasSize(1);
        assertThat(privateKeyWrapper.getPropertySpecs().get(0).getDisplayName()).isEqualTo("Private key");
        assertThat(privateKeyWrapper.getPropertySpecs()
                .get(0)
                .getDescription()).isEqualTo("Plaintext view of private key");
        assertThat(privateKeyWrapper.getPropertySpecs()
                .get(0)
                .getValueFactory()
                .getValueType()).isEqualTo(String.class);
    }

    @Test
    @Transactional
    public void testCreateDSAPlaintextPrivateKey() throws
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException, NoSuchProviderException {
        KeyType keyType = inMemoryPersistence.getPkiService()
                .newAsymmetricKeyType("Some DSA key")
                .DSA()
                .keySize(512)
                .add();

        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getKeyType()).thenReturn(keyType);
        when(keyAccessorType.getKeyEncryptionMethod()).thenReturn(DataVaultPrivateKeyFactory.KEY_ENCRYPTION_METHOD);
        PrivateKeyWrapper privateKeyWrapper = inMemoryPersistence.getPkiService().newPrivateKeyWrapper(keyAccessorType);
        privateKeyWrapper.renewValue();

        assertThat(privateKeyWrapper.getPrivateKey().getEncoded()).isNotEmpty();
        assertThat(privateKeyWrapper.getPrivateKey().getAlgorithm()).isEqualTo("DSA");
        assertThat(privateKeyWrapper.getPrivateKey().getFormat()).isEqualTo("PKCS#8");
        assertThat(privateKeyWrapper.getProperties()).hasSize(1);
        assertThat(privateKeyWrapper.getProperties()).containsKey("privateKey");
        assertThat(privateKeyWrapper.getPropertySpecs()).hasSize(1);
        assertThat(privateKeyWrapper.getPropertySpecs().get(0).getDisplayName()).isEqualTo("Private key");
        assertThat(privateKeyWrapper.getPropertySpecs()
                .get(0)
                .getDescription()).isEqualTo("Plaintext view of private key");
        assertThat(privateKeyWrapper.getPropertySpecs()
                .get(0)
                .getValueFactory()
                .getValueType()).isEqualTo(String.class);
    }

    @Test
    @Transactional
    public void testCreatePlaintextSymmetricAesKey() {
        KeyType created = inMemoryPersistence.getPkiService().newSymmetricKeyType("AES128B", "AES", 128).add();
        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getKeyType()).thenReturn(created);
        when(keyAccessorType.getKeyEncryptionMethod()).thenReturn(DataVaultSymmetricKeyFactory.KEY_ENCRYPTION_METHOD);
        PlaintextSymmetricKey symmetricKeyWrapper = (PlaintextSymmetricKey) inMemoryPersistence.getPkiService()
                .newSymmetricKeyWrapper(keyAccessorType);
        symmetricKeyWrapper.renewValue();

        Assertions.assertThat(symmetricKeyWrapper.getKey().getEncoded()).isNotEmpty();
        Assertions.assertThat(symmetricKeyWrapper.getKey().getAlgorithm()).isEqualTo("AES");
        Assertions.assertThat(symmetricKeyWrapper.getKey().getFormat()).isEqualTo("RAW");
        Assertions.assertThat(symmetricKeyWrapper.getProperties()).hasSize(1);
        Assertions.assertThat(symmetricKeyWrapper.getProperties()).containsKey("key");
        Assertions.assertThat(symmetricKeyWrapper.getPropertySpecs()).hasSize(1);
        Assertions.assertThat(symmetricKeyWrapper.getPropertySpecs().get(0).getDisplayName()).isEqualTo("key");
        Assertions.assertThat(symmetricKeyWrapper.getPropertySpecs().get(0).getDescription())
                .isEqualTo("Plaintext view of key");
        Assertions.assertThat(symmetricKeyWrapper.getPropertySpecs().get(0).getValueFactory().getValueType())
                .isEqualTo(String.class);
    }

    @Test
    @Transactional
    public void testCreateTrustedCertificate() throws Exception {
        TrustStore main = inMemoryPersistence.getPkiService()
                .newTrustStore("main")
                .description("Main trust store")
                .add();
        X509Certificate certificate = loadCertificate("myRootCA.cert");
        main.addCertificate(certificate);

        Optional<TrustStore> reloaded = inMemoryPersistence.getPkiService().findTrustStore("main");
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getDescription()).isEqualTo("Main trust store");
        assertThat(reloaded.get().getName()).isEqualTo("main");
        List<TrustedCertificate> certificates = reloaded.get().getCertificates();
        assertThat(certificates).hasSize(1);
        assertThat(certificates.get(0).getCertificate()).isPresent();
        assertThat(certificates.get(0).getCertificate().get().getIssuerDN().getName()).isEqualTo("CN=MyRootCA, OU=SmartEnergy, O=Honeywell, L=Kortrijk, ST=Vlaanderen, C=BE");
        assertThat(certificates.get(0).getCertificate().get().getSubjectDN().getName()).isEqualTo("CN=MyRootCA, OU=SmartEnergy, O=Honeywell, L=Kortrijk, ST=Vlaanderen, C=BE");

    }

    private X509Certificate createSelfSignedCertificate(String myself) throws Exception {
        // generate a key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(4096, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // build a certificate generator
        X500Name dnName = new X500Name("cn="+myself);
        Date notBefore = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        Date notAfter = new Date(System.currentTimeMillis() + 2 * 365 * 24 * 60 * 60 * 1000);

        SubjectPublicKeyInfo subjectPublicKeyInfo = new SubjectPublicKeyInfo(AlgorithmIdentifier.getInstance("SHA256WithRSA"), keyPair
                .getPublic()
                .getEncoded());
        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(dnName, BigInteger.TEN, notBefore, notAfter, dnName, subjectPublicKeyInfo);
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA")
                .build(keyPair.getPrivate());
        X509Certificate certificate = new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certificateBuilder.build(contentSigner));
        return certificate;
    }

    private X509Certificate loadCertificate(String name) throws IOException, CertificateException {
        return (X509Certificate) certificateFactory.generateCertificate(CertPathValidatorTest.class.getResourceAsStream(name));
    }


}
