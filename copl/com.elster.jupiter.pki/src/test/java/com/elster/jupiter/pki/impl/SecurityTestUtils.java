/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.util.Pair;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
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
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;

public class SecurityTestUtils {
    private static CertificateFactory certificateFactory;
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static CertificateFactory getCertificateFactory() throws CertificateException, NoSuchProviderException {
        if (certificateFactory == null) {
            certificateFactory = CertificateFactory.getInstance("X.509", "BC");
        }
        return certificateFactory;
    }

    public static Pair<X509Certificate, PrivateKey> generateSelfSignedCertificate(String cn) throws
            NoSuchProviderException,
            NoSuchAlgorithmException,
            OperatorCreationException, CertificateException, IOException, SignatureException, InvalidKeyException {
        // generate a key pair
        KeyPair keyPair = generateKeyPair();

        // build a certificate generator
        X509Certificate certificate = generateAndSignX509Certificate(cn, keyPair.getPublic(), cn, keyPair.getPrivate());
        return Pair.of(certificate, keyPair.getPrivate());
    }

    public static Pair<X509Certificate, PrivateKey> generateCertificate(String subjectDN, String issuerDN, PrivateKey privateKey) throws
            NoSuchProviderException,
            NoSuchAlgorithmException,
            OperatorCreationException, CertificateException, IOException, SignatureException, InvalidKeyException {
        // generate a key pair
        KeyPair keyPair = generateKeyPair();

        // build a certificate generator
        X509Certificate certificate = generateAndSignX509Certificate(issuerDN, keyPair.getPublic(), subjectDN, privateKey);
        return Pair.of(certificate, keyPair.getPrivate());
    }

    public static X509Certificate signCSR(PKCS10CertificationRequest csr, String issuerDN, PrivateKey privateKey) throws
            NoSuchProviderException,
            NoSuchAlgorithmException,
            OperatorCreationException, CertificateException, IOException, SignatureException, InvalidKeyException {
        return generateAndSignX509Certificate(issuerDN, new JcaPEMKeyConverter().getPublicKey(csr.getSubjectPublicKeyInfo()), csr.getSubject().toString(), privateKey);
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

    private static X509Certificate generateAndSignX509Certificate(String issuerDN, PublicKey publicKey, String subjectDN, PrivateKey signingPrivateKey) throws
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

        info.set(X509CertInfo.VALIDITY, interval);
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
        info.set(X509CertInfo.SUBJECT, new X500Name(subjectDN));
        info.set(X509CertInfo.ISSUER, new X500Name(issuerDN));
        info.set(X509CertInfo.KEY, new CertificateX509Key(publicKey));
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        AlgorithmId algo = new AlgorithmId(AlgorithmId.sha256WithRSAEncryption_oid);
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));

        // Sign the cert to identify the algorithm that's used.
        X509CertImpl cert = new X509CertImpl(info);
        cert.sign(signingPrivateKey, "sha256withrsa");

        // Update the algorithm, and resign.
        algo = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG);
        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
        cert = new X509CertImpl(info);
        cert.sign(signingPrivateKey, "sha256withrsa");
        return cert;
    }

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(2048, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    public static InputStream getAsStream(String name) {
        return SecurityTestUtils.class.getResourceAsStream(name);
    }

    public static X509Certificate loadCertificate(String name) throws CertificateException, NoSuchProviderException {
        return (X509Certificate) getCertificateFactory().generateCertificate(getAsStream(name));
    }
}
