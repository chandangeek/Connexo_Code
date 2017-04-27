package com.elster.jupiter.pki.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.ExtendedKeyUsage;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.KeyUsage;
import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.PlaintextPrivateKeyWrapper;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.pki.impl.wrappers.asymmetric.DataVaultPrivateKeyFactory;
import com.elster.jupiter.pki.impl.wrappers.symmetric.DataVaultPassphraseFactory;
import com.elster.jupiter.pki.impl.wrappers.symmetric.DataVaultSymmetricKeyFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;

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
import org.bouncycastle.operator.OperatorCreationException;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

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
        ((PkiServiceImpl) inMemoryPersistence.getPkiService()).addPassphraseFactory(inMemoryPersistence.getDataVaultPassphraseFactory());
        Security.addProvider(new BouncyCastleProvider());
        certificateFactory = CertificateFactory.getInstance("X.509", "BC");
    }

    @After
    public void tearDown() throws Exception {
        ((PkiServiceImpl) inMemoryPersistence.getPkiService()).removePrivateKeyFactory(inMemoryPersistence.getDataVaultPrivateKeyFactory());
        ((PkiServiceImpl) inMemoryPersistence.getPkiService()).removeSymmetricKeyFactory(inMemoryPersistence.getDataVaultSymmetricKeyFactory());
        ((PkiServiceImpl) inMemoryPersistence.getPkiService()).removePassphraseFactory(inMemoryPersistence.getDataVaultPassphraseFactory());
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
    public void testCreatePassphraseKeyType() {
        KeyType created = inMemoryPersistence.getPkiService()
                .newPassphraseType("Basic")
                .withUpperCaseCharacters()
                .withLowerCaseCharacters()
                .withNumbers()
                .withSpecialCharacters()
                .length(20)
                .description("hello")
                .add();
        Optional<KeyType> keyType = inMemoryPersistence.getPkiService().getKeyType("Basic");
        assertThat(keyType).isPresent();
        assertThat(keyType.get().getName()).isEqualTo("Basic");
        assertThat(keyType.get().getCryptographicType()).isEqualTo(CryptographicType.Passphrase);
        assertThat(keyType.get().getPasswordLength()).isEqualTo(20);
        assertThat(keyType.get().getDescription()).isEqualTo("hello");
        assertThat(keyType.get().useLowerCaseCharacters()).isEqualTo(true);
        assertThat(keyType.get().useUpperCaseCharacters()).isEqualTo(true);
        assertThat(keyType.get().useNumbers()).isEqualTo(true);
        assertThat(keyType.get().useSpecialCharacters()).isEqualTo(true);
        assertThat(keyType.get().getCurve()).isNull();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+MessageSeeds.Keys.INVALIDPASSPHRASELENGTH+"}")
    public void testCreatePassphraseKeyTypeWithInvalidLength() {
        KeyType created = inMemoryPersistence.getPkiService()
                .newPassphraseType("Basic")
                .withUpperCaseCharacters()
                .withLowerCaseCharacters()
                .withNumbers()
                .withSpecialCharacters()
                .description("hello")
                .add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+MessageSeeds.Keys.NOVALIDCHARACTERS+"}")
    public void testCreatePassphraseKeyTypeNoChars() {
        KeyType created = inMemoryPersistence.getPkiService()
                .newPassphraseType("Basic")
                .length(100)
                .description("hello")
                .add();
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

        PrivateKeyWrapper privateKeyWrapper = inMemoryPersistence.getPkiService().newPrivateKeyWrapper(keyType, DataVaultPrivateKeyFactory.KEY_ENCRYPTION_METHOD);
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

        PrivateKeyWrapper privateKeyWrapper = inMemoryPersistence.getPkiService().newPrivateKeyWrapper(keyType, DataVaultPrivateKeyFactory.KEY_ENCRYPTION_METHOD);
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

        PrivateKeyWrapper privateKeyWrapper = inMemoryPersistence.getPkiService().newPrivateKeyWrapper(keyType, DataVaultPrivateKeyFactory.KEY_ENCRYPTION_METHOD);
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
        when(keyAccessorType.getDuration()).thenReturn(Optional.of(TimeDuration.years(2)));
        when(keyAccessorType.getKeyEncryptionMethod()).thenReturn(DataVaultSymmetricKeyFactory.KEY_ENCRYPTION_METHOD);
        PlaintextSymmetricKey symmetricKeyWrapper = (PlaintextSymmetricKey) inMemoryPersistence.getPkiService()
                .newSymmetricKeyWrapper(keyAccessorType);
        symmetricKeyWrapper.generateValue();

        assertThat(symmetricKeyWrapper.getKey()).isPresent();
        assertThat(symmetricKeyWrapper.getKey().get().getEncoded()).isNotEmpty();
        assertThat(symmetricKeyWrapper.getKey().get().getAlgorithm()).isEqualTo("AES");
        assertThat(symmetricKeyWrapper.getKey().get().getFormat()).isEqualTo("RAW");
        assertThat(symmetricKeyWrapper.getProperties()).hasSize(1);
        assertThat(symmetricKeyWrapper.getProperties()).containsKey("key");
        assertThat(symmetricKeyWrapper.getPropertySpecs()).hasSize(1);
        assertThat(symmetricKeyWrapper.getPropertySpecs().get(0).getDisplayName()).isEqualTo("Key");
        assertThat(symmetricKeyWrapper.getPropertySpecs().get(0).getDescription()).isEqualTo("Base64 encoded key");
        assertThat(symmetricKeyWrapper.getPropertySpecs().get(0).getValueFactory().getValueType()).isEqualTo(String.class);
        assertThat(symmetricKeyWrapper.getExpirationTime()).isPresent();
        assertThat(symmetricKeyWrapper.getExpirationTime().get()).isEqualTo(ZonedDateTime.of(2019, 4, 4, 13, 0,0,0, ZoneId.of("UTC")).toInstant());
    }

    @Test
    @Transactional
    public void testGeneratePlaintextPasswordKey() {
        KeyType created = inMemoryPersistence.getPkiService().newPassphraseType("SECRET").withLowerCaseCharacters().withUpperCaseCharacters().length(20).add();
        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getKeyType()).thenReturn(created);
        when(keyAccessorType.getDuration()).thenReturn(Optional.of(TimeDuration.years(2)));
        when(keyAccessorType.getKeyEncryptionMethod()).thenReturn(DataVaultPassphraseFactory.KEY_ENCRYPTION_METHOD);
        PlaintextPassphrase passphraseWrapper = (PlaintextPassphrase) inMemoryPersistence.getPkiService()
                .newPassphraseWrapper(keyAccessorType);
        passphraseWrapper.generateValue();

        assertThat(passphraseWrapper.getPassphrase()).isPresent();
        assertThat(passphraseWrapper.getPassphrase().get()).isNotEmpty();
        assertThat(passphraseWrapper.getPassphrase().get()).hasSize(20);
        assertThat(passphraseWrapper.getProperties()).hasSize(1);
        assertThat(passphraseWrapper.getProperties()).containsKey("passphrase");
        assertThat(((String)passphraseWrapper.getProperties().get("passphrase"))).hasSize(20);
        assertThat(passphraseWrapper.getPropertySpecs()).hasSize(1);
        assertThat(passphraseWrapper.getPropertySpecs().get(0).getDisplayName()).isEqualTo("Passphrase");
        assertThat(passphraseWrapper.getPropertySpecs().get(0).getDescription()).isEqualTo("Plaintext passphrase");
        assertThat(passphraseWrapper.getPropertySpecs().get(0).getValueFactory().getValueType()).isEqualTo(String.class);
        assertThat(passphraseWrapper.getExpirationTime()).isPresent();
        assertThat(passphraseWrapper.getExpirationTime().get()).isEqualTo(ZonedDateTime.of(2019, 4, 4, 13, 0,0,0, ZoneId.of("UTC")).toInstant());
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
        assertThat(certificates.get(0).getCertificate().get().getIssuerDN().getName()).contains("CN=MyRootCA", "OU=SmartEnergy", "O=Honeywell", "L=Kortrijk", "ST=Vlaanderen", "C=BE");
        assertThat(certificates.get(0).getCertificate().get().getSubjectDN().getName()).contains("CN=MyRootCA", "OU=SmartEnergy", "O=Honeywell", "L=Kortrijk", "ST=Vlaanderen", "C=BE");

        assertThat(certificates.get(0).getStatus()).isEqualTo(TranslationKeys.AVAILABLE.getDefaultFormat());
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
        assertThat(reloaded.get().getStatus()).isEqualTo("Available");
        assertThat(reloaded.get().getAllKeyUsages()).isPresent();
        assertThat(reloaded.get().getAllKeyUsages().get()).contains("digitalSignature", "keyAgreement", "tlsWebServerAuthentication", "tlsWebClientAuthentication");
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+MessageSeeds.Keys.FIELD_TOO_LONG+"}", property="alias")
    public void testImportCertificateWithLargeAlias() throws Exception {
        StringBuilder alias = new StringBuilder();
        IntStream.range(1,260).forEach(i->alias.append("A")); // max == 256
        inMemoryPersistence.getPkiService().newCertificateWrapper(alias.toString());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+MessageSeeds.Keys.FIELD_TOO_LONG+"}", property="alias")
    public void testImportCertificateWithHugeAlias_CXO_6591() throws Exception {
        StringBuilder alias = new StringBuilder();
        IntStream.range(1,5000).forEach(i->alias.append("A"));
        inMemoryPersistence.getPkiService().newCertificateWrapper(alias.toString());
    }

    @Test
    @Transactional
    public void testImportCertificate_CXO_6608() throws Exception {
        X509Certificate certificate = loadCertificate("TestCSR2.cert.der");
        CertificateWrapper certificateWrapper = inMemoryPersistence.getPkiService().newCertificateWrapper("cxo-6608");
        certificateWrapper.setCertificate(certificate);

        Optional<CertificateWrapper> reloaded = inMemoryPersistence.getPkiService().findCertificateWrapper("cxo-6608");
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getCertificate()).isPresent();
        assertThat(reloaded.get().getStatus()).isEqualTo("Available");
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
        ClientCertificateWrapper comserver = inMemoryPersistence.getPkiService()
                .newClientCertificateWrapper(certificateType, "DataVault").alias("comserver-cc").add();
        Optional<ClientCertificateWrapper> comserver1 = inMemoryPersistence.getPkiService()
                .findClientCertificateWrapper("comserver-cc");
        assertThat(comserver1).isPresent();
    }

    @Test
    @Transactional
    public void testCreateCsrForECKey() throws Exception {
        KeyType certificateType = inMemoryPersistence.getPkiService()
                .newClientCertificateType("TLS-EC", "SHA256withECDSA")
                .setKeyUsages(EnumSet.of(KeyUsage.cRLSign))
                .setExtendedKeyUsages(EnumSet.of(ExtendedKeyUsage.digitalSignature, ExtendedKeyUsage.tlsWebClientAuthentication))
                .ECDSA()
                .curve("secp256r1")
                .add();
        ClientCertificateWrapper clientCertificateWrapper = inMemoryPersistence.getPkiService().newClientCertificateWrapper(certificateType, "DataVault").alias("comserver").add();
        clientCertificateWrapper.getPrivateKeyWrapper().generateValue();

        X500NameBuilder x500NameBuilder = new X500NameBuilder();
        x500NameBuilder.addRDN(BCStyle.CN, "ComserverTlsClient");
        clientCertificateWrapper.generateCSR(x500NameBuilder.build());

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
    public void testCreatePasswordWrapper() throws Exception {
        KeyType passwordType = inMemoryPersistence.getPkiService()
                .newPassphraseType("Setec Astronomy")
                .withUpperCaseCharacters()
                .withLowerCaseCharacters()
                .length(120)
                .add();
        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getKeyType()).thenReturn(passwordType);
        when(keyAccessorType.getDuration()).thenReturn(Optional.of(TimeDuration.years(1)));
        when(keyAccessorType.getKeyEncryptionMethod()).thenReturn(DataVaultPassphraseFactory.KEY_ENCRYPTION_METHOD);

        PlaintextPassphrase passphraseWrapper = (PlaintextPassphrase) inMemoryPersistence.getPkiService().newPassphraseWrapper(keyAccessorType);
        passphraseWrapper.generateValue();

        assertThat(passphraseWrapper.getPassphrase()).isPresent();
        String password = passphraseWrapper.getPassphrase().get();
        assertThat(password).hasSize(120);
        assertThat(password).matches(Pattern.compile("[a-zA-Z]{120}"));
    }

    @Test
    @Transactional
    public void testExtensionsOnCSR() throws Exception {
        KeyType certificateType = inMemoryPersistence.getPkiService()
                .newClientCertificateType("TLS-EC-2", "SHA256withECDSA")
                .setKeyUsages(EnumSet.of(KeyUsage.cRLSign, KeyUsage.decipherOnly))
                .setExtendedKeyUsages(EnumSet.of(ExtendedKeyUsage.digitalSignature, ExtendedKeyUsage.tlsWebClientAuthentication))
                .ECDSA()
                .curve("secp256r1")
                .add();

        ClientCertificateWrapper clientCertificateWrapper = inMemoryPersistence.getPkiService().newClientCertificateWrapper(certificateType, "DataVault").alias("comsrvr").add();
        clientCertificateWrapper.getPrivateKeyWrapper().generateValue();

        X500NameBuilder x500NameBuilder = new X500NameBuilder();
        x500NameBuilder.addRDN(BCStyle.CN, "ComserverTlsClient");
        clientCertificateWrapper.generateCSR(x500NameBuilder.build());

        // Assertions
        assertThat(clientCertificateWrapper.getCSR()).isPresent();
        assertThat(clientCertificateWrapper.getAllKeyUsages()).isPresent();
        assertThat(clientCertificateWrapper.getAllKeyUsages().get()).contains("digitalSignature");
        assertThat(clientCertificateWrapper.getAllKeyUsages().get()).contains("decipherOnly");
        assertThat(clientCertificateWrapper.getAllKeyUsages().get()).contains("cRLSign");
        assertThat(clientCertificateWrapper.getAllKeyUsages().get()).contains("tlsWebClientAuthentication");
        assertThat(clientCertificateWrapper.getKeyUsages()).containsOnly(KeyUsage.cRLSign, KeyUsage.decipherOnly);
        assertThat(clientCertificateWrapper.getExtendedKeyUsages()).containsOnly(ExtendedKeyUsage.digitalSignature, ExtendedKeyUsage.tlsWebClientAuthentication);
    }

    @Test
    @Transactional
    public void testCreateCsrForRSAKey() throws Exception {
        KeyType certificateType = inMemoryPersistence.getPkiService()
                .newClientCertificateType("TLS-RSA", "SHA256withRSA")
                .RSA()
                .keySize(1024)
                .add();

        ClientCertificateWrapper clientCertificateWrapper = inMemoryPersistence.getPkiService().newClientCertificateWrapper(certificateType, "DataVault").alias("comserver-rsa").add();
        clientCertificateWrapper.getPrivateKeyWrapper().generateValue();

        X500NameBuilder x500NameBuilder = new X500NameBuilder();
        x500NameBuilder.addRDN(BCStyle.CN, "ComserverTlsClient");
        clientCertificateWrapper.generateCSR(x500NameBuilder.build());

        Optional<ClientCertificateWrapper> reloaded = inMemoryPersistence.getPkiService()
                .findClientCertificateWrapper("comserver-rsa");
        // Assertions
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getCSR()).isPresent();
        BcPKCS10CertificationRequest bcPkcs10 = new BcPKCS10CertificationRequest(reloaded.get().getCSR().get().toASN1Structure());
        assertThat(bcPkcs10.getSubject().toString()).contains("CN=ComserverTlsClient");
        ContentVerifierProvider verifierProvider = new JcaContentVerifierProviderBuilder().build(((PlaintextPrivateKeyWrapper) reloaded.get()
                .getPrivateKeyWrapper()).getPublicKey());
        boolean signatureValid = bcPkcs10.isSignatureValid(verifierProvider);
        assertThat(signatureValid).isTrue();
    }

    @Test
    @Transactional
    public void testImportCertificateForExistingCsr() throws Exception {
        KeyType certificateType = inMemoryPersistence.getPkiService()
                .newClientCertificateType("TLS-RSA-Import", "SHA256withRSA")
                .RSA()
                .keySize(1024)
                .add();

        ClientCertificateWrapper clientCertificateWrapper = inMemoryPersistence.getPkiService().newClientCertificateWrapper(certificateType, "DataVault").alias("comserver-import").add();
        clientCertificateWrapper.getPrivateKeyWrapper().generateValue();

        X500NameBuilder x500NameBuilder = new X500NameBuilder();
        x500NameBuilder.addRDN(BCStyle.CN, "ComserverTlsClient");
        x500NameBuilder.addRDN(BCStyle.C, "Belgium");
        x500NameBuilder.addRDN(BCStyle.L, "kortrijk");
        x500NameBuilder.addRDN(BCStyle.O, "Honeywell");
        x500NameBuilder.addRDN(BCStyle.OU, "SmartEnergy");
        clientCertificateWrapper.generateCSR(x500NameBuilder.build());

        X509Certificate certificate = generateCertificateFromCSR(x500NameBuilder, clientCertificateWrapper.getCSR().get().getSubjectPublicKeyInfo());
        clientCertificateWrapper.setCertificate(certificate);
        // Assertions
        Optional<ClientCertificateWrapper> reloaded = inMemoryPersistence.getPkiService()
                .findClientCertificateWrapper("comserver-import");
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getCSR()).isPresent();
        assertThat(reloaded.get().getCertificate()).isPresent();
    }

    @Test
    @Transactional
    @Expected(value = PkiLocalizedException.class, message = "The certificate's subject distinguished name does not match the CSR")
    public void testImportCertificateForExistingCsrWithSubjectDnMismatch() throws Exception {
        KeyType certificateType = inMemoryPersistence.getPkiService()
                .newClientCertificateType("TLS-DN-MISMATCH", "SHA256withRSA")
                .RSA()
                .keySize(1024)
                .add();

        ClientCertificateWrapper clientCertificateWrapper = inMemoryPersistence.getPkiService().newClientCertificateWrapper(certificateType, "DataVault").alias("comserver-dn-mismatch").add();
        clientCertificateWrapper.getPrivateKeyWrapper().generateValue();

        X500NameBuilder x500NameBuilder = new X500NameBuilder();
        x500NameBuilder.addRDN(BCStyle.CN, "ComserverTlsClient");
        x500NameBuilder.addRDN(BCStyle.C, "Belgium");
        x500NameBuilder.addRDN(BCStyle.L, "kortrijk");
        x500NameBuilder.addRDN(BCStyle.O, "Honeywell");
        x500NameBuilder.addRDN(BCStyle.OU, "SmartEnergy");
        clientCertificateWrapper.generateCSR(x500NameBuilder.build());

        X500NameBuilder newName = new X500NameBuilder();
        newName.addRDN(BCStyle.CN, "ComserverTlsClient");
        newName.addRDN(BCStyle.C, "Belgium");
        newName.addRDN(BCStyle.O, "Honeywell");
        newName.addRDN(BCStyle.OU, "SmartEnergy");

        X509Certificate certificate = generateCertificateFromCSR(newName, clientCertificateWrapper.getCSR().get().getSubjectPublicKeyInfo());
        clientCertificateWrapper.setCertificate(certificate);
    }

    @Test
    @Transactional
    @Expected(value = PkiLocalizedException.class, message = "The certificate's key usage extension does not match the CSR")
    public void testImportCertificateForExistingCsrWithKeyUsageMismatch() throws Exception {
        KeyType certificateType = inMemoryPersistence.getPkiService()
                .newClientCertificateType("TLS-DN-KEYUSAGE", "SHA256withRSA")
                .setKeyUsages(EnumSet.of(KeyUsage.digitalSignature))
                .RSA()
                .keySize(1024)
                .add();

        ClientCertificateWrapper clientCertificateWrapper = inMemoryPersistence.getPkiService().newClientCertificateWrapper(certificateType, "DataVault").alias("comserver-ku-mismatch").add();
        clientCertificateWrapper.getPrivateKeyWrapper().generateValue();

        X500NameBuilder x500NameBuilder = new X500NameBuilder();
        x500NameBuilder.addRDN(BCStyle.CN, "ComserverTlsClient");
        x500NameBuilder.addRDN(BCStyle.C, "Belgium");
        x500NameBuilder.addRDN(BCStyle.L, "kortrijk");
        x500NameBuilder.addRDN(BCStyle.O, "Honeywell");
        x500NameBuilder.addRDN(BCStyle.OU, "SmartEnergy");
        clientCertificateWrapper.generateCSR(x500NameBuilder.build());

        X509Certificate certificate = generateCertificateFromCSR(x500NameBuilder, clientCertificateWrapper.getCSR().get().getSubjectPublicKeyInfo());
        clientCertificateWrapper.setCertificate(certificate);
    }

    @Test
    @Transactional
    @Expected(value = PkiLocalizedException.class, message = "The certificate's extended key usage extension does not match the CSR")
    public void testImportCertificateForExistingCsrWithExtendedKeyUsageMismatch() throws Exception {
        KeyType certificateType = inMemoryPersistence.getPkiService()
                .newClientCertificateType("TLS-DN-EXTENDEDKEYUSAGE", "SHA256withRSA")
                .setExtendedKeyUsages(EnumSet.of(ExtendedKeyUsage.emailProtection))
                .RSA()
                .keySize(1024)
                .add();

        ClientCertificateWrapper clientCertificateWrapper = inMemoryPersistence.getPkiService().newClientCertificateWrapper(certificateType, "DataVault").alias("comserver-eku-mismatch").add();
        clientCertificateWrapper.getPrivateKeyWrapper().generateValue();

        X500NameBuilder x500NameBuilder = new X500NameBuilder();
        x500NameBuilder.addRDN(BCStyle.CN, "ComserverTlsClient");
        x500NameBuilder.addRDN(BCStyle.C, "Belgium");
        x500NameBuilder.addRDN(BCStyle.L, "kortrijk");
        x500NameBuilder.addRDN(BCStyle.O, "Honeywell");
        x500NameBuilder.addRDN(BCStyle.OU, "SmartEnergy");
        clientCertificateWrapper.generateCSR(x500NameBuilder.build());

        X509Certificate certificate = generateCertificateFromCSR(x500NameBuilder, clientCertificateWrapper.getCSR().get().getSubjectPublicKeyInfo());
        clientCertificateWrapper.setCertificate(certificate);
    }

    @Test
    @Transactional
    @Expected(value = PkiLocalizedException.class, message = "The certificate's public key does not match the CSR")
    public void testImportMismatchingCertificateForExistingCsr() throws Exception {
        KeyType certificateType = inMemoryPersistence.getPkiService()
                .newClientCertificateType("TLS-RSA-mismatch", "SHA256withRSA")
                .RSA()
                .keySize(1024)
                .add();
        KeyAccessorType certificateAccessorType = mock(KeyAccessorType.class);
        when(certificateAccessorType.getKeyType()).thenReturn(certificateType);
        when(certificateAccessorType.getKeyEncryptionMethod()).thenReturn("DataVault");

        ClientCertificateWrapper clientCertificateWrapper = inMemoryPersistence.getPkiService().newClientCertificateWrapper(certificateType, "DataVault").alias("import-mismatch").add();
        clientCertificateWrapper.getPrivateKeyWrapper().generateValue();

        X500NameBuilder x500NameBuilder = new X500NameBuilder();
        x500NameBuilder.addRDN(BCStyle.CN, "ComserverTlsClient");
        clientCertificateWrapper.generateCSR(x500NameBuilder.build());
        PKCS10CertificationRequest originalCSR = clientCertificateWrapper.getCSR().get();

        clientCertificateWrapper.getPrivateKeyWrapper().generateValue(); // NEW PK !!
        clientCertificateWrapper.generateCSR(x500NameBuilder.build()); // NEW CSR !!

        X509Certificate certificate = generateCertificateFromCSR(x500NameBuilder, originalCSR.getSubjectPublicKeyInfo());
        clientCertificateWrapper.setCertificate(certificate); // Sets Certificate for original CSR, not most recent
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

        ClientCertificateWrapper clientCertificateWrapper = inMemoryPersistence.getPkiService().newClientCertificateWrapper(certificateType, "DataVault").alias("comserver-dsa").add();
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

        ClientCertificateWrapper clientCertificateWrapper = inMemoryPersistence.getPkiService().newClientCertificateWrapper(certificateType, "DataVault").alias("comserver-dup").add();
        ClientCertificateWrapper duplicate = inMemoryPersistence.getPkiService().newClientCertificateWrapper(certificateType, "DataVault").alias("comserver-dup").add();
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

        ClientCertificateWrapper clientCertificateWrapper = inMemoryPersistence.getPkiService().newClientCertificateWrapper(certificateType, "DataVault").alias("comserver-dup1").add();
        CertificateWrapper duplicate = inMemoryPersistence.getPkiService().newCertificateWrapper("comserver-dup1");
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
        ClientCertificateWrapper clientCertificateWrapper = inMemoryPersistence.getPkiService().newClientCertificateWrapper(certificateType, "DataVault").alias("myCert3").add();
    }

    @Test
    @Transactional
    public void testGetPropertySpecsCertificate() throws Exception {
        KeyType certificateType = inMemoryPersistence.getPkiService()
                .newClientCertificateType("TLS-props", "SHA256withDSA")
                .DSA()
                .keySize(512)
                .add();
        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getKeyType()).thenReturn(certificateType);
        when(keyAccessorType.getKeyEncryptionMethod()).thenReturn("DataVault");
        List<PropertySpec> propertySpecs = inMemoryPersistence.getPkiService().getPropertySpecs(keyAccessorType);

        assertThat(propertySpecs).hasSize(1);
        assertThat(propertySpecs.get(0).getName()).isEqualTo("alias");
        assertThat(propertySpecs.get(0).getDisplayName()).isEqualTo("Alias");
    }

    @Test
    @Transactional
    public void testGetPropertySpecsTrustedCertificate() throws Exception {
        TrustStore main = inMemoryPersistence.getPkiService()
                .newTrustStore("daverit")
                .description("Main trust store")
                .add();
        X509Certificate x509Certificate = loadCertificate("myRootCA.cert");
        main.addCertificate("myCert", x509Certificate);

        Optional<TrustStore> reloaded = inMemoryPersistence.getPkiService().findTrustStore("daverit");
        TrustedCertificate certificate = reloaded.get().getCertificates().get(0);
        List<PropertySpec> propertySpecs = certificate.getPropertySpecs();
        assertThat(propertySpecs).hasSize(2);
        assertThat(propertySpecs.get(0).getName()).isEqualTo("alias");
        assertThat(propertySpecs.get(0).getDisplayName()).isEqualTo("Alias");

        assertThat(propertySpecs.get(1).getName()).isEqualTo("trustStore");
        assertThat(propertySpecs.get(1).getDisplayName()).isEqualTo("Trust store");

        Map<String, Object> properties = certificate.getProperties();
        assertThat(((TrustStore)properties.get("trustStore")).getName()).isEqualTo(main.getName());
        assertThat(((TrustStore)properties.get("trustStore")).getId()).isEqualTo(main.getId());
    }

    @Test
    @Transactional
    public void testGetPropertySpecsSymmetricKey() throws Exception {
        KeyType created = inMemoryPersistence.getPkiService().newSymmetricKeyType("AES128-props", "AES", 128).add();
        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getKeyType()).thenReturn(created);
        when(keyAccessorType.getKeyEncryptionMethod()).thenReturn("DataVault");
        List<PropertySpec> propertySpecs = inMemoryPersistence.getPkiService().getPropertySpecs(keyAccessorType);

        assertThat(propertySpecs).hasSize(1);
        assertThat(propertySpecs.get(0).getName()).isEqualTo("key");
        assertThat(propertySpecs.get(0).getDisplayName()).isEqualTo("Key");
    }

    @Test
    @Transactional
    public void testGetAndUpdatePropertiesSymmetricKey() throws Exception {
        KeyType created = inMemoryPersistence.getPkiService().newSymmetricKeyType("AES128-props-update", "AES", 128).add();
        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getKeyType()).thenReturn(created);
        when(keyAccessorType.getKeyEncryptionMethod()).thenReturn("DataVault");
        when(keyAccessorType.getDuration()).thenReturn(Optional.of(TimeDuration.years(2)));

        SymmetricKeyWrapper symmetricKeyWrapper = inMemoryPersistence.getPkiService()
                .newSymmetricKeyWrapper(keyAccessorType);
        symmetricKeyWrapper.generateValue();

        Map<String, Object> properties = symmetricKeyWrapper.getProperties();
        assertThat(properties).containsKeys("key");
        assertThat((String)properties.get("key")).isNotEmpty();

        symmetricKeyWrapper.setProperties(properties);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+MessageSeeds.Keys.INVALID_VALUE+"}", property="key")
    public void testUpdatePropertiesSymmetricKeyWithImproperBase64() throws Exception {
        KeyType created = inMemoryPersistence.getPkiService().newSymmetricKeyType("AES128-props-base64", "AES", 128).add();
        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getKeyType()).thenReturn(created);
        when(keyAccessorType.getKeyEncryptionMethod()).thenReturn("DataVault");
        when(keyAccessorType.getDuration()).thenReturn(Optional.of(TimeDuration.years(1)));

        SymmetricKeyWrapper symmetricKeyWrapper = inMemoryPersistence.getPkiService()
                .newSymmetricKeyWrapper(keyAccessorType);

        Map<String, Object> map = new HashMap<>();
        map.put("key", "not really base64 is it");
        symmetricKeyWrapper.setProperties(map);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+MessageSeeds.Keys.INVALID_KEY_SIZE+"}", property="key")
    public void testUpdatePropertiesSymmetricKeyWithImproperSecretKey() throws Exception {
        KeyType created = inMemoryPersistence.getPkiService().newSymmetricKeyType("AES128-props-sk", "AES", 128).add();
        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getKeyType()).thenReturn(created);
        when(keyAccessorType.getKeyEncryptionMethod()).thenReturn("DataVault");
        when(keyAccessorType.getDuration()).thenReturn(Optional.of(TimeDuration.years(1)));

        SymmetricKeyWrapper symmetricKeyWrapper = inMemoryPersistence.getPkiService()
                .newSymmetricKeyWrapper(keyAccessorType);

        Map<String, Object> map = new HashMap<>();
        map.put("key", new String(Base64.getEncoder().encode(new byte[]{1,2,3,4,5}))); // incorrect symmetric key
        symmetricKeyWrapper.setProperties(map);
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

    private X509Certificate generateCertificateFromCSR(X500NameBuilder x500NameBuilder, SubjectPublicKeyInfo subjectPublicKeyInfo) throws
            NoSuchAlgorithmException,
            NoSuchProviderException,
            OperatorCreationException,
            CertificateException {
        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(x500NameBuilder.build(), BigInteger.TEN, new Date(), new Date(), x500NameBuilder.build(), subjectPublicKeyInfo);
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(4096, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());
        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certificateBuilder.build(contentSigner));
    }


}
