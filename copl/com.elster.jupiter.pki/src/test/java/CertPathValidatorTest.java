import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.security.Security;
import java.security.cert.CRL;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by bvn on 1/10/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class CertPathValidatorTest {

    private CertificateFactory certificateFactory;

    @Before
    public void setUp() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        certificateFactory = CertificateFactory.getInstance("X.509", "BC");
    }

    @Test
    public void testValidValidateCertChain() throws Exception {
        X509Certificate root = loadCertificate("certpathvalidator/myRootCA.cert");
        X509Certificate subCa = loadCertificate("certpathvalidator/mySubCA.cert");
        X509Certificate device = loadCertificate("certpathvalidator/myDevice.cert");

        TrustAnchor rootTrustAnchor = new TrustAnchor(root, null);
        TrustAnchor subCaTrustAnchor = new TrustAnchor(subCa, null);
        Set<TrustAnchor> trustAnchors = new HashSet<>();
        trustAnchors.add(rootTrustAnchor);
        trustAnchors.add(subCaTrustAnchor);
        PKIXParameters pkixParameters = new PKIXParameters(trustAnchors);
        pkixParameters.setRevocationEnabled(false); //No CRL support available yet

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        CertPath path = certFactory.generateCertPath(Arrays.asList(device));

        CertPathValidator validator = CertPathValidator.getInstance("PKIX");    //PKIX algorithm validates CertPath objects of type X.509
        CertPathValidatorResult validate = validator.validate(path, pkixParameters);
        System.out.println(validate);
    }

    /**
     * Certificate generated with 'openssl ca', should be valid
     */
    @Test
    public void testValidOpenSSLCAGeneratedCertificateValidateCertChain() throws Exception {
        X509Certificate root = loadCertificate("certpathvalidator/myRootCA.cert");
        X509Certificate subCa = loadCertificate("certpathvalidator/mySubCA.cert");
        X509Certificate device = loadCertificate("certpathvalidator/myDevice.2.cert");

        TrustAnchor rootTrustAnchor = new TrustAnchor(root, null);
        TrustAnchor subCaTrustAnchor = new TrustAnchor(subCa, null);
        Set<TrustAnchor> trustAnchors = new HashSet<>();
        trustAnchors.add(rootTrustAnchor);
        trustAnchors.add(subCaTrustAnchor);
        PKIXParameters pkixParameters = new PKIXParameters(trustAnchors);
        pkixParameters.setRevocationEnabled(false); //No CRL support available yet

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        CertPath path = certFactory.generateCertPath(Arrays.asList(device));

        CertPathValidator validator = CertPathValidator.getInstance("PKIX");    //PKIX algorithm validates CertPath objects of type X.509
        CertPathValidatorResult validate = validator.validate(path, pkixParameters);
        System.out.println(validate);
    }

    /**
     * Certificate generated with 'openssl ca', should be valid
     */
    @Test
    public void testRevokedCertificateValidateCertChain() throws Exception {
        X509Certificate root = loadCertificate("certpathvalidator/myRootCA.cert");
        X509Certificate subCa = loadCertificate("certpathvalidator/mySubCA.cert");
        X509Certificate device = loadCertificate("certpathvalidator/myDevice.2.cert");
        CRL crl = certificateFactory.generateCRL(this.getClass()
                .getResourceAsStream("certpathvalidator/mySubCA.revoked.crl.pem"));

        TrustAnchor rootTrustAnchor = new TrustAnchor(root, null);
        TrustAnchor subCaTrustAnchor = new TrustAnchor(subCa, null);
        Set<TrustAnchor> trustAnchors = new HashSet<>();
        trustAnchors.add(rootTrustAnchor);
        trustAnchors.add(subCaTrustAnchor);

        PKIXParameters pkixParameters = new PKIXParameters(trustAnchors);
        CertStoreParameters ccsp = new CollectionCertStoreParameters(Arrays.asList(root, subCa, crl));
        CertStore store = CertStore.getInstance("Collection", ccsp);
        pkixParameters.addCertStore(store);
        pkixParameters.setRevocationEnabled(true);

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        CertPath path = certFactory.generateCertPath(Arrays.asList(device));

        CertPathValidator validator = CertPathValidator.getInstance("PKIX");    //PKIX algorithm validates CertPath objects of type X.509
        CertPathValidatorResult validate = validator.validate(path, pkixParameters);
        System.out.println(validate);
    }

    @Test(expected = CertPathValidatorException.class)
    public void testInvalidValidateCertChain() throws Exception {
        X509Certificate root = loadCertificate("certpathvalidator/myRootCA.cert");
        X509Certificate subCa = loadCertificate("certpathvalidator/mySubCA.cert");
        X509Certificate device = loadCertificate("certpathvalidator/fakeDevice.cert");

        TrustAnchor rootTrustAnchor = new TrustAnchor(root, null);
        TrustAnchor subCaTrustAnchor = new TrustAnchor(subCa, null);
        Set<TrustAnchor> trustAnchors = new HashSet<>();
        trustAnchors.add(rootTrustAnchor);
        trustAnchors.add(subCaTrustAnchor);
        PKIXParameters pkixParameters = new PKIXParameters(trustAnchors);
        pkixParameters.setRevocationEnabled(false); //No CRL support available yet

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        CertPath path = certFactory.generateCertPath(Arrays.asList(device));

        CertPathValidator validator = CertPathValidator.getInstance("PKIX");    //PKIX algorithm validates CertPath objects of type X.509
        CertPathValidatorResult validate = validator.validate(path, pkixParameters);
    }

    private X509Certificate loadCertificate(String name) throws IOException, CertificateException {
        return (X509Certificate) certificateFactory.generateCertificate(this.getClass().getResourceAsStream(name));
    }
}
