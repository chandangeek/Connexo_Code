package com.elster.jupiter.pki.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.ExtendedKeyUsage;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.KeyUsage;
import com.elster.jupiter.pki.PlaintextPrivateKeyWrapper;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.pki.impl.wrappers.asymmetric.DataVaultPrivateKeyFactory;
import com.elster.jupiter.pki.impl.wrappers.symmetric.DataVaultSymmetricKeyFactory;

import certpathvalidator.CertPathValidatorTest;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcECContentVerifierProviderBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.bc.BcPKCS10CertificationRequest;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.EnumSet;
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
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PKIServiceImplIT {

    private static PkiInMemoryPersistence inMemoryPersistence = new PkiInMemoryPersistence();

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
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
    public void testCreateSymmetricKeyType() {
        KeyType created = inMemoryPersistence.getPkiService()
                .newSymmetricKeyType("AES128", "AES", 128)
                .description("hello")
                .add();
        Optional<KeyType> keyType = inMemoryPersistence.getPkiService().getKeyType("AES128");
        assertThat(keyType).isPresent();
        assertThat(keyType.get().getName()).isEqualTo("AES128");
        assertThat(keyType.get().getKeyAlgorithm()).isEqualTo("AES");
        assertThat(keyType.get().getCryptographicType()).isEqualTo(CryptographicType.SymmetricKey);
        assertThat(keyType.get().getKeySize()).isEqualTo(128);
        assertThat(keyType.get().getDescription()).isEqualTo("hello");
        assertThat(keyType.get().getCurve()).isNull();
    }

    @Test
    @Transactional
    public void testCreateCertificateWithRSAKeyType() {
        KeyType created = inMemoryPersistence.getPkiService()
                .newClientCertificateType("RSA2048", "SHA256withRSA")
                .description("boe")
                .RSA()
                .keySize(2048)
                .add();
        Optional<KeyType> keyType = inMemoryPersistence.getPkiService().getKeyType("RSA2048");
        assertThat(keyType).isPresent();
        assertThat(keyType.get().getName()).isEqualTo("RSA2048");
        assertThat(keyType.get().getKeyAlgorithm()).isEqualTo("RSA");
        assertThat(keyType.get().getSignatureAlgorithm()).isEqualTo("SHA256withRSA");
        assertThat(keyType.get().getDescription()).isEqualTo("boe");
        assertThat(keyType.get().getCryptographicType()).isEqualTo(CryptographicType.ClientCertificate);
        assertThat(keyType.get().getKeySize()).isEqualTo(2048);
        assertThat(keyType.get().getCurve()).isNull();
    }

    @Test
    @Transactional
    public void testCreateClientCertificateDSAKeyType() {
        KeyType created = inMemoryPersistence.getPkiService()
                .newClientCertificateType("DSA1024", "SHA256withDSA")
                .DSA().keySize(1024).add();
        Optional<KeyType> keyType = inMemoryPersistence.getPkiService().getKeyType("DSA1024");
        assertThat(keyType).isPresent();
        assertThat(keyType.get().getName()).isEqualTo("DSA1024");
        assertThat(keyType.get().getKeyAlgorithm()).isEqualTo("DSA");
        assertThat(keyType.get().getSignatureAlgorithm()).isEqualTo("SHA256withDSA");
        assertThat(keyType.get().getCryptographicType()).isEqualTo(CryptographicType.ClientCertificate);
        assertThat(keyType.get().getKeySize()).isEqualTo(1024);
        assertThat(keyType.get().getCurve()).isNull();
    }

    @Test
    @Transactional
    public void testCreateECKeyType() {
        KeyType created = inMemoryPersistence.getPkiService()
                .newClientCertificateType("NIST P-256", "SHA256withECDSA")
                .description("check")
                .ECDSA()
                .curve("secp256r1")
                .add();
        Optional<KeyType> keyType = inMemoryPersistence.getPkiService().getKeyType("NIST P-256");
        assertThat(keyType).isPresent();
        assertThat(keyType.get().getName()).isEqualTo("NIST P-256");
        assertThat(keyType.get().getKeyAlgorithm()).isEqualTo("ECDSA");
        assertThat(keyType.get().getSignatureAlgorithm()).isEqualTo("SHA256withECDSA");
        assertThat(keyType.get().getDescription()).isEqualTo("check");
        assertThat(keyType.get().getCryptographicType()).isEqualTo(CryptographicType.ClientCertificate);
        assertThat(keyType.get().getKeySize()).isEqualTo(0); // CXO-5375, I expect null here
        assertThat(keyType.get().getCurve()).isEqualTo("secp256r1");
    }

    @Test
    @Transactional
    public void testCreateClientCertificateTypeWithKeyUsages() throws Exception {
        inMemoryPersistence.getPkiService()
                .newClientCertificateType("TLS Server", "SHA256withRSA")
                .description("Example client cert")
                .setKeyUsages(EnumSet.of(KeyUsage.keyAgreement, KeyUsage.keyCertSign))
                .setExtendedKeyUsages(EnumSet.of(ExtendedKeyUsage.tlsWebClientAuthentication, ExtendedKeyUsage.tlsWebServerAuthentication))
                .ECDSA()
                .curve("secp256r1")
                .add();

        Optional<KeyType> keyType = inMemoryPersistence.getPkiService().getKeyType("TLS Server");
        assertThat(keyType).isPresent();
        assertThat(keyType.get().getName()).isEqualTo("TLS Server");
        assertThat(keyType.get().getKeyAlgorithm()).isEqualTo("ECDSA");
        assertThat(keyType.get().getSignatureAlgorithm()).isEqualTo("SHA256withRSA");
        assertThat(keyType.get().getDescription()).isEqualTo("Example client cert");
        assertThat(keyType.get().getKeyUsages()).containsOnly(KeyUsage.keyAgreement, KeyUsage.keyCertSign);
        assertThat(keyType.get().getExtendedKeyUsages()).containsOnly(ExtendedKeyUsage.tlsWebServerAuthentication, ExtendedKeyUsage.tlsWebClientAuthentication);
    }

    @Test
    @Transactional
    public void testGenerateECPlaintextPrivateKey() throws
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException, NoSuchProviderException {
        KeyType keyType = inMemoryPersistence.getPkiService()
                .newClientCertificateType("NIST P-256K", "SHA256withECDSA")
                .ECDSA()
                .curve("secp256k1")
                .add();

        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getKeyType()).thenReturn(keyType);
        when(keyAccessorType.getKeyEncryptionMethod()).thenReturn(DataVaultPrivateKeyFactory.KEY_ENCRYPTION_METHOD);
        PrivateKeyWrapper privateKeyWrapper = inMemoryPersistence.getPkiService().newPrivateKeyWrapper(keyAccessorType);
        privateKeyWrapper.generateValue();

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
    public void testGenerateRSAPlaintextPrivateKey() throws
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException, NoSuchProviderException {
        KeyType keyType = inMemoryPersistence.getPkiService()
                .newClientCertificateType("Some RSA key", "SHA256withRSA")
                .RSA()
                .keySize(2048)
                .add();

        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getKeyType()).thenReturn(keyType);
        when(keyAccessorType.getKeyEncryptionMethod()).thenReturn(DataVaultPrivateKeyFactory.KEY_ENCRYPTION_METHOD);
        PrivateKeyWrapper privateKeyWrapper = inMemoryPersistence.getPkiService().newPrivateKeyWrapper(keyAccessorType);
        privateKeyWrapper.generateValue();

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
                .newClientCertificateType("Some DSA key", "sha256withDSA")
                .DSA()
                .keySize(512)
                .add();

        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getKeyType()).thenReturn(keyType);
        when(keyAccessorType.getKeyEncryptionMethod()).thenReturn(DataVaultPrivateKeyFactory.KEY_ENCRYPTION_METHOD);
        PrivateKeyWrapper privateKeyWrapper = inMemoryPersistence.getPkiService().newPrivateKeyWrapper(keyAccessorType);
        privateKeyWrapper.generateValue();

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
    public void testGeneratePlaintextSymmetricAesKey() {
        KeyType created = inMemoryPersistence.getPkiService().newSymmetricKeyType("AES128B", "AES", 128).add();
        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getKeyType()).thenReturn(created);
        when(keyAccessorType.getKeyEncryptionMethod()).thenReturn(DataVaultSymmetricKeyFactory.KEY_ENCRYPTION_METHOD);
        PlaintextSymmetricKey symmetricKeyWrapper = (PlaintextSymmetricKey) inMemoryPersistence.getPkiService()
                .newSymmetricKeyWrapper(keyAccessorType);
        symmetricKeyWrapper.generateValue();

        Assertions.assertThat(symmetricKeyWrapper.getKey()).isPresent();
        Assertions.assertThat(symmetricKeyWrapper.getKey().get().getEncoded()).isNotEmpty();
        Assertions.assertThat(symmetricKeyWrapper.getKey().get().getAlgorithm()).isEqualTo("AES");
        Assertions.assertThat(symmetricKeyWrapper.getKey().get().getFormat()).isEqualTo("RAW");
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
        main.addCertificate("myCert", certificate);

        Optional<TrustStore> reloaded = inMemoryPersistence.getPkiService().findTrustStore("main");
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getDescription()).isEqualTo("Main trust store");
        assertThat(reloaded.get().getName()).isEqualTo("main");
        List<TrustedCertificate> certificates = reloaded.get().getCertificates();
        assertThat(certificates).hasSize(1);
        assertThat(certificates.get(0).getAlias()).isEqualTo("myCert");
        assertThat(certificates.get(0).getCertificate()).isPresent();
        assertThat(certificates.get(0).getCertificate().get().getIssuerDN().getName()).isEqualTo("CN=MyRootCA, OU=SmartEnergy, O=Honeywell, L=Kortrijk, ST=Vlaanderen, C=BE");
        assertThat(certificates.get(0).getCertificate().get().getSubjectDN().getName()).isEqualTo("CN=MyRootCA, OU=SmartEnergy, O=Honeywell, L=Kortrijk, ST=Vlaanderen, C=BE");

        assertThat(certificates.get(0).getStatus()).isEqualTo(TranslationKeys.PRESENT.getDefaultFormat());
        assertThat(certificates.get(0).getAllKeyUsages()).isPresent();
        assertThat(certificates.get(0).getAllKeyUsages().get()).isEmpty();
    }

    @Test
    @Transactional
    public void testCreateTrustStoreFromKeyStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        keyStore.load(this.getClass().getResourceAsStream("SM2016MDMCA-chain.jks"), "changeit".toCharArray());
        TrustStore main = inMemoryPersistence.getPkiService()
                .newTrustStore("imported")
                .description("Imported from keystore")
                .add();
        main.loadKeyStore(keyStore);

        Optional<TrustStore> loaded = inMemoryPersistence.getPkiService().findTrustStore("imported");
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getCertificates()).hasSize(2);
        assertThat(loaded.get().getCertificates().stream().map(CertificateWrapper::getAlias).collect(toList())).containsOnly("sm_2016_mdm_ca", "sm_2016_root_ca");
    }

    @Test
    @Transactional
    public void testCreateTrustStoreFromKeyStoreWithDuplicateAliases() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        keyStore.load(this.getClass().getResourceAsStream("SM2016MDMCA-chain.jks"), "changeit".toCharArray());
        TrustStore main = inMemoryPersistence.getPkiService()
                .newTrustStore("duplicates")
                .description("Imported from keystore")
                .add();
        main.loadKeyStore(keyStore);
        main.loadKeyStore(keyStore); // <-- DUPLICATES

        Optional<TrustStore> loaded = inMemoryPersistence.getPkiService().findTrustStore("duplicates");
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getCertificates()).hasSize(2);
        assertThat(loaded.get().getCertificates().stream().map(CertificateWrapper::getAlias).collect(toList())).containsOnly("sm_2016_mdm_ca", "sm_2016_root_ca");
    }

    @Test
    @Transactional
    public void testAddCRLtoTrustedCertificate() throws Exception {
        TrustStore main = inMemoryPersistence.getPkiService()
                .newTrustStore("CRL")
                .description("Main trust store")
                .add();
        X509Certificate certificate = loadCertificate("myRootCA.cert");
        TrustedCertificate trustedCertificate = main.addCertificate("myRootCA", certificate);

        trustedCertificate.setCRL(certificateFactory.generateCRL(CertPathValidatorTest.class.getResourceAsStream("mySubCA.revoked.crl.pem")));
        Optional<TrustStore> reloaded = inMemoryPersistence.getPkiService().findTrustStore("CRL");
        assertThat(reloaded).isPresent();
        TrustedCertificate trustedCertificateReloaded = reloaded.get().getCertificates().get(0);
        assertThat(trustedCertificateReloaded.getCRL()).isPresent();
    }

    @Test
    @Transactional
    public void testImportCertificate() throws Exception {
        X509Certificate certificate = loadCertificate("bvn.cert");
        CertificateWrapper certificateWrapper = inMemoryPersistence.getPkiService().newCertificateWrapper("bvn");
        certificateWrapper.setCertificate(certificate);

        Optional<CertificateWrapper> reloaded = inMemoryPersistence.getPkiService().findCertificateWrapper("bvn");
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getCertificate()).isPresent();
        assertThat(reloaded.get().getStatus()).isEqualTo("Present");
        assertThat(reloaded.get().getAllKeyUsages()).isPresent();
        assertThat(reloaded.get().getAllKeyUsages().get()).contains("digitalSignature", "keyAgreement", "tlsWebServerAuthentication", "tlsWebClientAuthentication");
    }

    @Test
    @Transactional
    public void testFindAndLockTrustStore() throws Exception {
        TrustStore main = inMemoryPersistence.getPkiService()
                .newTrustStore("LOCK")
                .description("Versioned trust store")
                .add();

        Optional<TrustStore> correct = inMemoryPersistence.getPkiService()
                .findAndLockTrustStoreByIdAndVersion(main.getId(), main.getVersion());

        assertThat(correct).isPresent();
    }

    @Test
    @Transactional
    public void testFindAndLockTrustStoreIncorrectVersion() throws Exception {
        TrustStore main = inMemoryPersistence.getPkiService()
                .newTrustStore("LOCK2")
                .description("Versioned trust store")
                .add();

        Optional<TrustStore> incorrect = inMemoryPersistence.getPkiService()
                .findAndLockTrustStoreByIdAndVersion(main.getId(), main.getVersion()+1);

        assertThat(incorrect).isEmpty();
    }

    @Test
    @Transactional
    public void testRemoveCertificateFromTrustStore() throws Exception {
        TrustStore main = inMemoryPersistence.getPkiService()
                .newTrustStore("DEL")
                .description("Main trust store")
                .add();
        X509Certificate certificate = loadCertificate("myRootCA.cert");
        main.addCertificate("MyRootCa", certificate);

        Optional<TrustStore> reloaded = inMemoryPersistence.getPkiService().findTrustStore("DEL");
        assertThat(reloaded.get().getCertificates()).hasSize(1);
        reloaded.get().removeCertificate("MyRootCa");
        Optional<TrustStore> rereloaded = inMemoryPersistence.getPkiService().findTrustStore("DEL");
        assertThat(rereloaded.get().getCertificates()).isEmpty();
    }

    @Test
    @Transactional
    public void testCreateClientCertificate() throws Exception {
        KeyType certificateType = inMemoryPersistence.getPkiService()
                .newClientCertificateType("TLS-CC", "SHA256withECDSA")
                .ECDSA()
                .curve("secp256r1")
                .add();
        KeyAccessorType certificateAccessorType = mock(KeyAccessorType.class);
        when(certificateAccessorType.getKeyType()).thenReturn(certificateType);
        when(certificateAccessorType.getKeyEncryptionMethod()).thenReturn("DataVault");
        ClientCertificateWrapper comserver = inMemoryPersistence.getPkiService()
                .newClientCertificateWrapper(certificateAccessorType);
        comserver.setAlias("comserver-cc");
        comserver.save();
        Optional<ClientCertificateWrapper> comserver1 = inMemoryPersistence.getPkiService()
                .findClientCertificateWrapper("comserver-cc");
        assertThat(comserver1).isPresent();
    }

    @Test
    @Transactional
    public void testCreateCsrForECKey() throws Exception {
        KeyType certificateType = inMemoryPersistence.getPkiService()
                .newClientCertificateType("TLS-EC", "SHA256withECDSA")
                .ECDSA()
                .curve("secp256r1")
                .add();
        KeyAccessorType certificateAccessorType = mock(KeyAccessorType.class);
        when(certificateAccessorType.getKeyType()).thenReturn(certificateType);
        when(certificateAccessorType.getKeyEncryptionMethod()).thenReturn("DataVault");

        ClientCertificateWrapper clientCertificateWrapper = inMemoryPersistence.getPkiService().newClientCertificateWrapper(certificateAccessorType);
        clientCertificateWrapper.setAlias("comserver");
        clientCertificateWrapper.save();
        clientCertificateWrapper.getPrivateKeyWrapper().generateValue();

        X500NameBuilder x500NameBuilder = new X500NameBuilder();
        x500NameBuilder.addRDN(BCStyle.CN, "ComserverTlsClient");
        PKCS10CertificationRequest pkcs10CertificationRequest = clientCertificateWrapper.getPrivateKeyWrapper()
                .generateCSR(x500NameBuilder.build(), certificateType.getSignatureAlgorithm());
        clientCertificateWrapper.setCSR(pkcs10CertificationRequest);
        clientCertificateWrapper.save();

        // Assertions
        assertThat(clientCertificateWrapper.getCSR()).isPresent();
        BcPKCS10CertificationRequest bcPkcs10 = new BcPKCS10CertificationRequest(clientCertificateWrapper.getCSR().get().toASN1Structure());
        assertThat(bcPkcs10.getSubject().toString()).contains("CN=ComserverTlsClient");
        ContentVerifierProvider verifierProvider = new BcECContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder())
                .build(bcPkcs10.getPublicKey());
        boolean signatureValid = bcPkcs10.isSignatureValid(verifierProvider);
        assertThat(signatureValid).isTrue();
    }

    @Test
    @Transactional
    public void testCreateCsrForRSAKey() throws Exception {
        KeyType certificateType = inMemoryPersistence.getPkiService()
                .newClientCertificateType("TLS-RSA", "SHA256withRSA")
                .RSA()
                .keySize(1024)
                .add();
        KeyAccessorType certificateAccessorType = mock(KeyAccessorType.class);
        when(certificateAccessorType.getKeyType()).thenReturn(certificateType);
        when(certificateAccessorType.getKeyEncryptionMethod()).thenReturn("DataVault");

        ClientCertificateWrapper clientCertificateWrapper = inMemoryPersistence.getPkiService().newClientCertificateWrapper(certificateAccessorType);
        clientCertificateWrapper.setAlias("comserver-rsa");
        clientCertificateWrapper.save();
        clientCertificateWrapper.getPrivateKeyWrapper().generateValue();

        X500NameBuilder x500NameBuilder = new X500NameBuilder();
        x500NameBuilder.addRDN(BCStyle.CN, "ComserverTlsClient");
        PKCS10CertificationRequest pkcs10CertificationRequest = clientCertificateWrapper.getPrivateKeyWrapper()
                .generateCSR(x500NameBuilder.build(), certificateType.getSignatureAlgorithm());
        clientCertificateWrapper.setCSR(pkcs10CertificationRequest);
        clientCertificateWrapper.save();

        // Assertions
        assertThat(clientCertificateWrapper.getCSR()).isPresent();
        BcPKCS10CertificationRequest bcPkcs10 = new BcPKCS10CertificationRequest(clientCertificateWrapper.getCSR().get().toASN1Structure());
        assertThat(bcPkcs10.getSubject().toString()).contains("CN=ComserverTlsClient");
        ContentVerifierProvider verifierProvider = new JcaContentVerifierProviderBuilder().build(((PlaintextPrivateKeyWrapper) clientCertificateWrapper
                .getPrivateKeyWrapper()).getPublicKey());
        boolean signatureValid = bcPkcs10.isSignatureValid(verifierProvider);
        assertThat(signatureValid).isTrue();
    }

    @Test
    @Transactional
    public void testCreateCsrForDSAKey() throws Exception {
        KeyType certificateType = inMemoryPersistence.getPkiService()
                .newClientCertificateType("TLS-DSA", "SHA256withDSA")
                .DSA()
                .keySize(512)
                .add();
        KeyAccessorType certificateAccessorType = mock(KeyAccessorType.class);
        when(certificateAccessorType.getKeyType()).thenReturn(certificateType);
        when(certificateAccessorType.getKeyEncryptionMethod()).thenReturn("DataVault");

        ClientCertificateWrapper clientCertificateWrapper = inMemoryPersistence.getPkiService().newClientCertificateWrapper(certificateAccessorType);
        clientCertificateWrapper.setAlias("comserver-dsa");
        clientCertificateWrapper.save();
        clientCertificateWrapper.getPrivateKeyWrapper().generateValue();

        X500NameBuilder x500NameBuilder = new X500NameBuilder();
        x500NameBuilder.addRDN(BCStyle.CN, "ComserverTlsClient");
        PKCS10CertificationRequest pkcs10CertificationRequest = clientCertificateWrapper.getPrivateKeyWrapper()
                .generateCSR(x500NameBuilder.build(), certificateType.getSignatureAlgorithm());
        clientCertificateWrapper.setCSR(pkcs10CertificationRequest);
        clientCertificateWrapper.save();

        // Assertions
        assertThat(clientCertificateWrapper.getCSR()).isPresent();
        BcPKCS10CertificationRequest bcPkcs10 = new BcPKCS10CertificationRequest(clientCertificateWrapper.getCSR().get().toASN1Structure());
        assertThat(bcPkcs10.getSubject().toString()).contains("CN=ComserverTlsClient");
        ContentVerifierProvider verifierProvider = new JcaContentVerifierProviderBuilder().build(((PlaintextPrivateKeyWrapper) clientCertificateWrapper
                .getPrivateKeyWrapper()).getPublicKey());
        boolean signatureValid = bcPkcs10.isSignatureValid(verifierProvider);
        assertThat(signatureValid).isTrue();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+MessageSeeds.Keys.ALIAS_UNIQUE+"}", property="alias")
    public void testDuplicateAliasForCertificate() throws Exception {
        KeyType certificateType = inMemoryPersistence.getPkiService()
                .newClientCertificateType("TLS-DUPLICATE", "SHA256withDSA")
                .DSA()
                .keySize(512)
                .add();
        KeyAccessorType certificateAccessorType = mock(KeyAccessorType.class);
        when(certificateAccessorType.getKeyType()).thenReturn(certificateType);
        when(certificateAccessorType.getKeyEncryptionMethod()).thenReturn("DataVault");

        ClientCertificateWrapper clientCertificateWrapper = inMemoryPersistence.getPkiService().newClientCertificateWrapper(certificateAccessorType);
        clientCertificateWrapper.setAlias("comserver-dup");
        clientCertificateWrapper.save();
        ClientCertificateWrapper duplicate = inMemoryPersistence.getPkiService().newClientCertificateWrapper(certificateAccessorType);
        duplicate.setAlias("comserver-dup");
        duplicate.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+MessageSeeds.Keys.ALIAS_UNIQUE+"}", property="alias")
    public void testDuplicateAliasForDifferentCertificates() throws Exception {
        KeyType certificateType = inMemoryPersistence.getPkiService()
                .newClientCertificateType("TLS-DUPLICATE2", "SHA256withDSA")
                .DSA()
                .keySize(512)
                .add();
        KeyAccessorType certificateAccessorType = mock(KeyAccessorType.class);
        when(certificateAccessorType.getKeyType()).thenReturn(certificateType);
        when(certificateAccessorType.getKeyEncryptionMethod()).thenReturn("DataVault");

        ClientCertificateWrapper clientCertificateWrapper = inMemoryPersistence.getPkiService().newClientCertificateWrapper(certificateAccessorType);
        clientCertificateWrapper.setAlias("comserver-dup1");
        clientCertificateWrapper.save();
        CertificateWrapper duplicate = inMemoryPersistence.getPkiService().newCertificateWrapper("comserver-dup1");
        duplicate.setAlias("comserver-dup1");
        duplicate.save();
    }

    @Test
    @Transactional
    public void testNoDuplicateAliasForCertificatesInKeyStores() throws Exception {
        TrustStore ts1 = inMemoryPersistence.getPkiService().newTrustStore("ts1").add();
        TrustStore ts2 = inMemoryPersistence.getPkiService().newTrustStore("ts2").add();
        KeyType certificateType = inMemoryPersistence.getPkiService()
                .newClientCertificateType("TLS-DUPLICATE3", "SHA256withDSA")
                .DSA()
                .keySize(512)
                .add();
        KeyAccessorType certificateAccessorType = mock(KeyAccessorType.class);
        when(certificateAccessorType.getKeyType()).thenReturn(certificateType);
        when(certificateAccessorType.getKeyEncryptionMethod()).thenReturn("DataVault");

        X509Certificate certificate = loadCertificate("myRootCA.cert");
        ts1.addCertificate("myCertDup", certificate);
        ts2.addCertificate("myCertDup", certificate);
    }

    @Test
    @Transactional
    public void testNoDuplicateAliasForCertificatesInAndOutOfKeyStore() throws Exception {
        TrustStore ts1 = inMemoryPersistence.getPkiService().newTrustStore("ts3").add();
        KeyType certificateType = inMemoryPersistence.getPkiService().newClientCertificateType("TLS-DUPLICATE4", "SHA256withDSA")
                .DSA()
                .keySize(512)
                .add();

        KeyAccessorType certificateAccessorType = mock(KeyAccessorType.class);
        when(certificateAccessorType.getKeyType()).thenReturn(certificateType);
        when(certificateAccessorType.getKeyEncryptionMethod()).thenReturn("DataVault");

        X509Certificate certificate = loadCertificate("myRootCA.cert");
        ts1.addCertificate("myCert3", certificate);
        ClientCertificateWrapper clientCertificateWrapper = inMemoryPersistence.getPkiService().newClientCertificateWrapper(certificateAccessorType);
        clientCertificateWrapper.setAlias("myCert3");
        clientCertificateWrapper.save();
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
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());
        X509Certificate certificate = new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certificateBuilder.build(contentSigner));
        return certificate;
    }

    private X509Certificate loadCertificate(String name) throws IOException, CertificateException {
        return (X509Certificate) certificateFactory.generateCertificate(CertPathValidatorTest.class.getResourceAsStream(name));
    }


}
