import sun.security.x509.X509CertImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by bvn on 1/10/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class CertPathValidatorTest {

    @Test
    public void testValidateCertChain() throws Exception {
        X509Certificate root = loadCertificate("certpathvalidator/myRootCA.cert");
        X509Certificate subCa = loadCertificate("certpathvalidator/mySubCA.cert");
        X509Certificate device = loadCertificate("certpathvalidator/myDevice.cert");


    }

    private X509Certificate loadCertificate(String name) throws IOException, CertificateException {
        byte[] bytes = Files.readAllBytes(Paths.get(this.getClass()
                .getClassLoader()
                .getResource(name).getPath()));
        return new X509CertImpl(bytes);
    }
}
