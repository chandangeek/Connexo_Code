package com.elster.jupiter.pki.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.util.Pair;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class TrustStoreImpl2IT {

    private static PkiInMemoryPersistence inMemoryPersistence = new PkiInMemoryPersistence();

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();
    @Rule
    public TestRule transactionalRule = new TransactionalRule(inMemoryPersistence.getTransactionService());
    Pair<X509Certificate, PrivateKey> rootCertificate;
    Pair<X509Certificate, PrivateKey> subCa1;
    Pair<X509Certificate, PrivateKey> subCa2;
    Pair<X509Certificate, PrivateKey> subCa3;
    Pair<X509Certificate, PrivateKey> device;

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence.activate();
    }

    @Before
    public void setUp() throws Exception {
        ((PkiServiceImpl) inMemoryPersistence.getPkiService()).addPrivateKeyFactory(inMemoryPersistence.getDataVaultPrivateKeyFactory());
        ((PkiServiceImpl) inMemoryPersistence.getPkiService()).addSymmetricKeyFactory(inMemoryPersistence.getDataVaultSymmetricKeyFactory());
        ((PkiServiceImpl) inMemoryPersistence.getPkiService()).addPassphraseFactory(inMemoryPersistence.getDataVaultPassphraseFactory());
        rootCertificate = generateSelfSignedCertificate("ROOT");
        subCa1 = generateCertificate("SubCA1", "ROOT", rootCertificate.getLast());
        subCa2 = generateCertificate("SubCA2", "ROOT", rootCertificate.getLast());
        subCa3 = generateCertificate("SubCA3", "ROOT", rootCertificate.getLast());
        device = generateCertificate("Device", "SubCA2", subCa2.getLast());
        Security.addProvider(new BouncyCastleProvider());
    }

    @After
    public void tearDown() throws Exception {
        ((PkiServiceImpl) inMemoryPersistence.getPkiService()).removePrivateKeyFactory(inMemoryPersistence.getDataVaultPrivateKeyFactory());
        ((PkiServiceImpl) inMemoryPersistence.getPkiService()).removeSymmetricKeyFactory(inMemoryPersistence.getDataVaultSymmetricKeyFactory());
        ((PkiServiceImpl) inMemoryPersistence.getPkiService()).removePassphraseFactory(inMemoryPersistence.getDataVaultPassphraseFactory());
    }

    @Test
    @Transactional
    public void testBasicValidation() {
        TrustStore trustStore = inMemoryPersistence.getPkiService().newTrustStore("main").add();
        trustStore.addCertificate("root", rootCertificate.getFirst());
        trustStore.addCertificate("SubCa1", subCa1.getFirst());
        trustStore.addCertificate("SubCa2", subCa2.getFirst());
        trustStore.addCertificate("SubCa3", subCa3.getFirst());
        try {
            trustStore.validate(device.getFirst());
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | CertificateException e) {
            fail("Should have been valid");
        }
    }

    @Test
    public void testCertificateValidateCertChain() throws Exception {
        X509Certificate root = rootCertificate.getFirst();
        System.out.println(root);
        X509Certificate subCa = subCa2.getFirst();
        System.out.println(subCa);
        X509Certificate deviceCert = device.getFirst();
        System.out.println(deviceCert);

        TrustAnchor rootTrustAnchor = new TrustAnchor(root, null);
        TrustAnchor subCaTrustAnchor = new TrustAnchor(subCa, null);
        Set<TrustAnchor> trustAnchors = new HashSet<>();
        trustAnchors.add(rootTrustAnchor);
        trustAnchors.add(subCaTrustAnchor);

        PKIXParameters pkixParameters = new PKIXParameters(trustAnchors);
        CertStoreParameters ccsp = new CollectionCertStoreParameters();
        CertStore store = CertStore.getInstance("Collection", ccsp);
        pkixParameters.addCertStore(store);
        pkixParameters.setRevocationEnabled(false);

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        CertPath path = certFactory.generateCertPath(Arrays.asList(deviceCert));

        CertPathValidator validator = CertPathValidator.getInstance("PKIX");    //PKIX algorithm validates CertPath objects of type X.509
        CertPathValidatorResult validate = validator.validate(path, pkixParameters);
    }


    private Pair<X509Certificate, PrivateKey> generateSelfSignedCertificate(String cn) throws
            NoSuchProviderException,
            NoSuchAlgorithmException,
            OperatorCreationException, CertificateException, IOException, SignatureException, InvalidKeyException {
        // generate a key pair
        KeyPair keyPair = generateKeyPair();

        // build a certificate generator
        X509Certificate certificate = generateAndSignX509Certificate(cn, keyPair.getPublic(), cn, keyPair.getPrivate());
        return Pair.of(certificate, keyPair.getPrivate());

    }

    Pair<X509Certificate, PrivateKey> generateCertificate(String cn, String issuer, PrivateKey privateKey) throws
            NoSuchProviderException,
            NoSuchAlgorithmException,
            OperatorCreationException, CertificateException, IOException, SignatureException, InvalidKeyException {
        // generate a key pair
        KeyPair keyPair = generateKeyPair();

        // build a certificate generator
        X509Certificate certificate = generateAndSignX509Certificate(issuer, keyPair.getPublic(), cn, privateKey);
        return Pair.of(certificate, keyPair.getPrivate());

    }

//    private X509Certificate generateAndSignX509Certificate(String issuer, PublicKey publicKey, String subject, ContentSigner contentSigner) throws
//            CertificateException, IOException {
//        X500Name subjectDN = new X500Name("cn=" + subject);
//        X500Name issuerDN = new X500Name("cn=" + issuer);
//
//        Date notBefore = Date.from(Instant.now().minusSeconds(1000));
//        Date notAfter = Date.from(Instant.now().plusSeconds(3600));
//
//        SubjectPublicKeyInfo subjectPublicKeyInfo = new SubjectPublicKeyInfo(new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256WithRSA"), publicKey.getEncoded());
//        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(issuerDN, BigInteger.TEN, notBefore, notAfter, subjectDN, subjectPublicKeyInfo);
//        X509CertificateHolder certificateHolder = certificateBuilder.build(contentSigner);
//
//        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
//        InputStream in = new ByteArrayInputStream(certificateHolder.getEncoded());
//        return (X509Certificate) certFactory.generateCertificate(in);
//    }

    private X509Certificate generateAndSignX509Certificate(String issuer, PublicKey publicKey, String subject, PrivateKey privateKey) throws
            CertificateException,
            IOException,
            NoSuchProviderException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            SignatureException {
        X509CertInfo info = new X509CertInfo();
        Date notBefore = Date.from(Instant.now().minusSeconds(1000));
        Date notAfter = Date.from(Instant.now().plusSeconds(3600));
        CertificateValidity interval = new CertificateValidity(notBefore, notAfter);
        BigInteger sn = new BigInteger(64, new SecureRandom());
        X500Name ownerDN = new X500Name("cn="+subject);
        X500Name issuerDN = new X500Name("cn="+issuer);

        info.set(X509CertInfo.VALIDITY, interval);
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
        info.set(X509CertInfo.SUBJECT, ownerDN);
        info.set(X509CertInfo.ISSUER, issuerDN);
        info.set(X509CertInfo.KEY, new CertificateX509Key(publicKey));
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        AlgorithmId algo = new AlgorithmId(AlgorithmId.sha256WithRSAEncryption_oid);
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));

        // Sign the cert to identify the algorithm that's used.
        X509CertImpl cert = new X509CertImpl(info);
        cert.sign(privateKey, "sha256withrsa");

        // Update the algorith, and resign.
        algo = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG);
        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
        cert = new X509CertImpl(info);
        cert.sign(privateKey, "sha256withrsa");
        return cert;    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(4096, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

}
