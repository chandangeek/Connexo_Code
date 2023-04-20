package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.impl.PkiInMemoryPersistence;
import com.elster.jupiter.pki.impl.SecurityTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CertificateExportProcessorTest {
    private byte [] resultBytes;
    private byte [] resultSignature;

    private Clock clock = Clock.fixed(ZonedDateTime.of(2015, 6, 4, 14, 20, 55, 115451452, TimeZoneNeutral.getMcMurdo()).toInstant(), TimeZoneNeutral.getMcMurdo());
    private CertificateExportTagReplacer tagReplacer;
    private static PkiInMemoryPersistence inMemoryPersistence = new PkiInMemoryPersistence();

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence.activate();
    }

    @AfterClass
    public static void uninstall(){
        inMemoryPersistence.deactivate();
    }

    @Before
    public void setUp() throws Exception {
        tagReplacer = new CertificateExportTagReplacer(clock);
    }

    @Test
    public void testExportCertificate() throws Exception {
        SecurityAccessor securityAccessor = mock(SecurityAccessor.class);
        ClientCertificateWrapper clientCertificateWrapper = mock(ClientCertificateWrapper.class);
        PrivateKeyWrapper privateKeyWrapper = mock(PrivateKeyWrapper.class);
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = new SecureRandom();
        keyGenerator.initialize(2048, random);
        KeyPair keyPair = keyGenerator.genKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        when(securityAccessor.getActualValue()).thenReturn(Optional.of(clientCertificateWrapper));
        when(clientCertificateWrapper.getPrivateKeyWrapper()).thenReturn(privateKeyWrapper);
        when(clientCertificateWrapper.hasPrivateKey()).thenReturn(true);
        when(privateKeyWrapper.getPrivateKey()).thenReturn(Optional.of(privateKey));
        CertificateExportDestination certificateExportDestination = mock(CertificateExportDestination.class);
        SecurityManagementService securityManagementService = inMemoryPersistence.getSecurityManagementService();
        doAnswer(invocationOnMock -> {
            final Object[] args = invocationOnMock.getArguments();
            resultBytes = (byte[]) args[0];
            resultSignature = (byte[]) args[1];
            return null;
        }).when(certificateExportDestination).export(any(), any());
        when(certificateExportDestination.getUrl()).thenReturn("sftp://user@host:22");
        when(certificateExportDestination.getLastExportedFileName()).thenReturn(Optional.of("file.signed"));

        Logger logger = mock(Logger.class);
        FileImportOccurrence fileImportOccurrence = mock(FileImportOccurrence.class);
        when(fileImportOccurrence.getLogger()).thenReturn(logger);
        CSRImporterLogger csrLogger = new CSRImporterLogger(fileImportOccurrence, NlsModule.FakeThesaurus.INSTANCE);

        Map<String,Map<String, X509Certificate>> dcerts = new LinkedHashMap<>();
        Map<String, X509Certificate> certs = new LinkedHashMap<>();
        certs.put("test", SecurityTestUtils.loadCertificate("myRootCA.cert"));
        dcerts.put("100001", certs);
        Map<String, Object> props = new HashMap<>();
        props.put(CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR.getPropertyKey(), securityAccessor);
        props.put(CSRImporterTranslatedProperty.EXPORT_FLAT_DIR.getPropertyKey(), false);
        props.put(CSRImporterTranslatedProperty.EXPORT_FLAT_DIR.getPropertyKey(), false);
        props.put(CSRImporterTranslatedProperty.CLIENT_TRUSTSTORE_MAPPING.getPropertyKey(), "");

        CertificateExportProcessor certificateExportProcessor = new CertificateExportProcessor(props, certificateExportDestination, securityManagementService, csrLogger);

        // business method
        certificateExportProcessor.processExport(dcerts);

        // assertions
        verify(logger).log(Level.INFO, "'file.signed' has been successfully exported to the destination 'sftp://user@host:22'.");

        int signatureSize = 256;
        Signature signer = Signature.getInstance("sha256withrsa");
        signer.initVerify(publicKey);
        byte[] allbytes = new byte[resultBytes.length + signatureSize];
        System.arraycopy(resultBytes, 0, allbytes, 0, resultBytes.length);
        System.arraycopy(resultSignature, 0, allbytes, resultBytes.length, resultSignature.length);
        signer.update(resultBytes);
        byte[] sign = new byte[signatureSize];
        System.arraycopy(allbytes, allbytes.length - signatureSize, sign, 0, signatureSize);
        signer.verify(sign);
    }

    @Test
    public void testSeconds() {
        String template = "aFlurryOfTextInter<sec>spersedWithTags";
        String expected = "aFlurryOfTextInter55spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testDate() {
        String template = "aFlurryOfTextInter<date>spersedWithTags";
        String expected = "aFlurryOfTextInter20150604spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testTime() {
        String template = "aFlurryOfTextInter<time>spersedWithTags";
        String expected = "aFlurryOfTextInter142055spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testMillisec() {
        String template = "aFlurryOfTextInter<millisec>spersedWithTags";
        String expected = "aFlurryOfTextInter115spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testDateYear() {
        String template = "aFlurryOfTextInter<dateyear>spersedWithTags";
        String expected = "aFlurryOfTextInter2015spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testDateMonth() {
        String template = "aFlurryOfTextInter<datemonth>spersedWithTags";
        String expected = "aFlurryOfTextInter06spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testDateDay() {
        String template = "aFlurryOfTextInter<dateday>spersedWithTags";
        String expected = "aFlurryOfTextInter04spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }

    @Test
    public void testDatePattern() {
        String template = "aFlurryOfTextInter<dateformat:qqan'text'>spersedWithTags";
        String expectedReplacement = "02PM115451452text";
        String expected = "aFlurryOfTextInter" + expectedReplacement + "spersedWithTags";

        assertThat(tagReplacer.replaceTags(template)).isEqualTo(expected);
    }
}
