package com.energyict.mdc.device.data.importers.impl.certificatesimport;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty;
import com.energyict.mdc.device.data.importers.impl.FileImportZipEntry;
import com.energyict.mdc.device.data.importers.impl.FileImportZipLogger;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceCertificatesImportProcessorTest {

    @Mock
    DeviceDataImporterContext context;

    @Mock
    DeviceService deviceService;

    @Mock
    SecurityManagementService securityManagementService;

    @Mock
    FileImportService fileImportService;

    @Mock
    FileImportZipLogger logger;

    @InjectMocks
    DeviceCertificatesImportProcessor processor;

    @Before
    public void beforeTest() {
        reset(deviceService, securityManagementService, fileImportService, logger);

        Thesaurus thesaurus = mock(Thesaurus.class, RETURNS_MOCKS);
        when(context.getThesaurus()).thenReturn(thesaurus);
        when(context.getDeviceService()).thenReturn(deviceService);
        when(context.getSecurityManagementService()).thenReturn(securityManagementService);
        when(context.getFileImportService()).thenReturn(fileImportService);

        Path path = mock(Path.class);
        when(fileImportService.getBasePath()).thenReturn(path);

        Map<String, Object> properties = new HashMap<>();
        properties.put(DeviceDataImporterProperty.SECURITY_ACCESSOR_MAPPING.getPropertyKey(), "");

        processor = new DeviceCertificatesImportProcessor(context, properties);
    }

    @Test
    public void testDeviceNotFound() {
        FileImportZipEntry importZipEntry = getValidZipEntry();

        when(deviceService.findDevicesBySerialNumber(anyString())).thenReturn(Collections.emptyList());

        processor.process(getZipFile("certificates.zip"), importZipEntry, logger);
        verify(logger).warning(MessageSeeds.CANNOT_FIND_DEVICE_WITH_COMMON_NAME, "454C536301A0B8C0",null, "fileName");
    }

    @Test
    public void testSecurityAccessorNotFound() {
        ZipFile zipFile = getZipFile("certificates.zip");
        FileImportZipEntry importZipEntry = getValidZipEntry();
        List<SecurityAccessorType> securityAccessorTypes = new ArrayList<>();

        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getSecurityAccessorTypes()).thenReturn(securityAccessorTypes);

        Device device = mock(Device.class);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceService.findDevicesBySerialNumber(anyString())).thenReturn(Collections.singletonList(mock(Device.class, RETURNS_MOCKS)));

        processor.process(zipFile, importZipEntry, logger);
        verify(logger).warning(MessageSeeds.CERTIFICATE_NO_SUCH_KEY_ACCESSOR_TYPE, importZipEntry.getFileName());
    }


    @Test
    public void testUpdateSecurityAccessorSkipWhenActualAndTempValueArePresent() throws Exception {
        DeviceCertificatesImportProcessorTest.MockBuilder builder = new DeviceCertificatesImportProcessorTest.MockBuilder().build("RSA");

        ZipFile zipFile = getZipFile("certificates.zip");
        FileImportZipEntry importZipEntry = getValidZipEntry();
        when(builder.getSecurityAccessor().getActualPassphraseWrapperReference()).thenReturn(Optional.of(builder.getCertificateWrapper()));
        when(builder.getSecurityAccessor().getTempValue()).thenReturn(Optional.of(builder.getCertificateWrapper()));

        processor.process(zipFile, importZipEntry, logger);
        verify(builder.getDevice(), times(2)).getSecurityAccessor(any(SecurityAccessorType.class));
        verify(builder.getSecurityAccessor(), never()).save();
    }

    @Test
    public void testUpdateSecurityAccessorSetTempValueWhenActualValueIsPresentAndTempValueIsNotPresent() throws Exception {
        DeviceCertificatesImportProcessorTest.MockBuilder builder = new DeviceCertificatesImportProcessorTest.MockBuilder().build("RSA");

        FileImportZipEntry importZipEntry = builder.getImportZipEntry();
        when(builder.getSecurityAccessor().getActualPassphraseWrapperReference()).thenReturn(Optional.of(builder.getCertificateWrapper()));
        when(builder.getSecurityAccessor().getTempValue()).thenReturn(Optional.empty());

        processor.process(builder.getZipFile(), importZipEntry, logger);
        verify(builder.getDevice(), times(2)).getSecurityAccessor(any(SecurityAccessorType.class));
        verify(builder.getSecurityAccessor()).setTempValue(any(SecurityValueWrapper.class));
        verify(builder.getSecurityAccessor()).save();
    }

    @Test
    public void testUpdateSecurityAccessorSetActualValueWhenActualValueIsNotPresentAndTempValueIsPresent() throws Exception {
        DeviceCertificatesImportProcessorTest.MockBuilder builder = new DeviceCertificatesImportProcessorTest.MockBuilder().build("RSA");

        FileImportZipEntry importZipEntry = builder.getImportZipEntry();
        when(builder.getSecurityAccessor().getActualPassphraseWrapperReference()).thenReturn(Optional.empty());
        when(builder.getSecurityAccessor().getTempValue()).thenReturn(Optional.of(builder.getCertificateWrapper()));

        processor.process(builder.getZipFile(), importZipEntry, logger);
        verify(builder.getDevice(), times(2)).getSecurityAccessor(any(SecurityAccessorType.class));
        verify(builder.getSecurityAccessor()).setActualPassphraseWrapperReference(any(SecurityValueWrapper.class));
        verify(builder.getSecurityAccessor()).save();
    }

    @Test
    public void testUpdateSecurityAccessorSetActualValueWhenNoValuesArePresent() throws Exception {
        DeviceCertificatesImportProcessorTest.MockBuilder builder = new DeviceCertificatesImportProcessorTest.MockBuilder().build("RSA");

        FileImportZipEntry importZipEntry = builder.getImportZipEntry();
        when(builder.getSecurityAccessor().getActualPassphraseWrapperReference()).thenReturn(Optional.empty());
        when(builder.getSecurityAccessor().getTempValue()).thenReturn(Optional.of(builder.getCertificateWrapper()));

        processor.process(builder.getZipFile(), importZipEntry, logger);
        verify(builder.getDevice(), times(2)).getSecurityAccessor(any(SecurityAccessorType.class));
        verify(builder.getSecurityAccessor()).setActualPassphraseWrapperReference(any(SecurityValueWrapper.class));
        verify(builder.getSecurityAccessor()).save();
    }

    private ZipFile getZipFile(String fileName) {
        try {
            return new ZipFile(getClass()
                    .getResource("/com/energyict/mdc/device/data/importers/impl/" + fileName)
                    .getFile());
        } catch (Exception e) {
            return null;
        }
    }

    private FileImportZipEntry getValidZipEntry() {
        return new FileImportZipEntry("0105425037010016213415730002",
                "fileName", new ZipEntry("0105425037010016213415730002/tls-cert-454C536301A0B8C0.pem"), "tls-cert");
    }

    private InputStream getCertificate(String certificateName) throws FileNotFoundException {
        return new FileInputStream(
                Thread.currentThread()
                        .getContextClassLoader()
                        .getResource("com/energyict/mdc/device/data/importers/impl/" + certificateName)
                        .getFile());
    }

    private Path getBasePath() throws FileNotFoundException {
        return new File(
                Thread.currentThread()
                        .getContextClassLoader()
                        .getResource("com/energyict/mdc/device/data/importers/impl/")
                        .getFile()).toPath();
    }

    private class MockBuilder {
        private FileImportZipEntry importZipEntry;
        private ZipFile zipFile;
        private SecurityAccessor securityAccessor;
        private ClientCertificateWrapper clientCertificateWrapper;
        private CertificateWrapper certificateWrapper;
        private SecurityManagementService.ClientCertificateWrapperBuilder certificateWrapperBuilder;
        private X509Certificate certificate;
        private Device device;

        public FileImportZipEntry getImportZipEntry() {
            return importZipEntry;
        }

        public ZipFile getZipFile() {
            return zipFile;
        }

        public SecurityAccessor getSecurityAccessor() {
            return securityAccessor;
        }

        public CertificateWrapper getCertificateWrapper() {
            return certificateWrapper;
        }

        public Device getDevice() {
            return device;
        }

        public DeviceCertificatesImportProcessorTest.MockBuilder build(String keyAlgorithm) throws IOException, URISyntaxException, CertificateException {
            String certificateFileName = "tls-cert-454C536301A0B8C0.pem";
            String securityAccessorTypeName = certificateFileName.split("\\.")[0];
            importZipEntry = getValidZipEntry();

            zipFile = mock(ZipFile.class);
            SecurityAccessorType securityAccessorType = mock(SecurityAccessorType.class);
            DeviceType deviceType = mock(DeviceType.class);
            securityAccessor = mock(SecurityAccessor.class);
            clientCertificateWrapper = mock(ClientCertificateWrapper.class);
            certificate = mock(X509Certificate.class);
            certificateWrapper = mock(CertificateWrapper.class);
            certificateWrapperBuilder = mock(SecurityManagementService.ClientCertificateWrapperBuilder.class);

            KeyType keyType = mock(KeyType.class);

            when(keyType.getKeyAlgorithm()).thenReturn(keyAlgorithm);
            device = mock(Device.class, RETURNS_MOCKS);

            List<SecurityAccessorType> securityAccessorTypes = Collections.singletonList(securityAccessorType);

            when(securityAccessorType.getName()).thenReturn(securityAccessorTypeName);
            when(zipFile.getInputStream(any(ZipEntry.class))).thenReturn(getCertificate(certificateFileName));
            when(device.getDeviceType()).thenReturn(deviceType);
            when(device.getSecurityAccessor(any(SecurityAccessorType.class))).thenReturn(Optional.of(securityAccessor));
            when(deviceType.getSecurityAccessorTypes()).thenReturn(securityAccessorTypes);
            when(securityAccessorType.getKeyType()).thenReturn(keyType);
            when(clientCertificateWrapper.getCertificate()).thenReturn(Optional.of(certificate));
            when(clientCertificateWrapper.getKeyType()).thenReturn(keyType);
            when(certificateWrapperBuilder.alias(anyString())).thenReturn(certificateWrapperBuilder);
            when(certificateWrapperBuilder.add()).thenReturn(clientCertificateWrapper);
            when(securityManagementService.findClientCertificateWrapper(any())).thenReturn(Optional.ofNullable(clientCertificateWrapper));
            when(securityManagementService.newClientCertificateWrapper(any(KeyType.class), anyString())).thenReturn(certificateWrapperBuilder);
            when(deviceService.findDevicesBySerialNumber(anyString())).thenReturn(Collections.singletonList(device));
            when(fileImportService.getBasePath()).thenReturn(getBasePath());

            return this;
        }
    }
}