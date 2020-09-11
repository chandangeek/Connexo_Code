/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.fileimport.ImportScheduleBuilder;
import com.elster.jupiter.fileimport.impl.FileImportServiceImpl;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.pki.impl.PkiInMemoryPersistence;
import com.elster.jupiter.pki.impl.SecurityManagementServiceImpl;
import com.elster.jupiter.pki.impl.SecurityTestUtils;
import com.elster.jupiter.pki.impl.TranslationKeys;
import com.elster.jupiter.pki.impl.wrappers.symmetric.DataVaultSymmetricKeyFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.time.Never;

import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;

import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;

import static org.assertj.core.api.Assertions.assertThat;

public class CSRImporterFactoryIT {
    private static PkiInMemoryPersistence pkiInMemoryPersistence = new PkiInMemoryPersistence();
    private static CSRImporterFactory csrImporterFactory;
    private static SecurityManagementService securityManagementService;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    @Rule
    public TestRule transactional = new TransactionalRule(pkiInMemoryPersistence.getTransactionService());

    @BeforeClass
    public static void setUp() {
        pkiInMemoryPersistence.activate();
        csrImporterFactory = pkiInMemoryPersistence.getCSRImporterFactory();
        securityManagementService = pkiInMemoryPersistence.getSecurityManagementService();
        ((SecurityManagementServiceImpl) securityManagementService).addPrivateKeyFactory(pkiInMemoryPersistence.getDataVaultPrivateKeyFactory());
        ((FileImportServiceImpl) pkiInMemoryPersistence.getFileImportService()).addFileImporter(pkiInMemoryPersistence.getCSRImporterFactory());
    }

    @AfterClass
    public static void tearDown() {
        ((SecurityManagementServiceImpl) securityManagementService).removePrivateKeyFactory(pkiInMemoryPersistence.getDataVaultPrivateKeyFactory());
        ((FileImportServiceImpl) pkiInMemoryPersistence.getFileImportService()).removeFileImporter(pkiInMemoryPersistence.getCSRImporterFactory());
        pkiInMemoryPersistence.deactivate();
    }

    @Test
    @Transactional
    public void testGetNames() {
        assertThat(csrImporterFactory.getApplicationName()).isEqualTo("SYS");
        assertThat(csrImporterFactory.getDestinationName()).isEqualTo(CSRImporterMessageHandlerFactory.DESTINATION_NAME);
        assertThat(csrImporterFactory.getName()).isEqualTo(TranslationKeys.CSR_IMPORTER.getKey());
        assertThat(csrImporterFactory.getDisplayName()).isEqualTo(TranslationKeys.CSR_IMPORTER.getDefaultFormat());
    }

    @Test
    @Transactional
    public void testPropertySpecNamesAndDescriptions() {
        List<PropertySpec> propertySpecs = csrImporterFactory.getPropertySpecs();
        assertThat(propertySpecs.stream()
                .map(PropertySpec::getName)
                .collect(Collectors.toList()))
                .containsOnly(Arrays.stream(CSRImporterTranslatedProperty.values())
                        .map(CSRImporterTranslatedProperty::getPropertyKey)
                        .filter(string -> !string.endsWith(".description"))
                        .toArray(String[]::new));
        Map<String, String> propertiesWithTranslations = Arrays.stream(CSRImporterTranslatedProperty.values())
                .filter(property -> !property.getKey().endsWith(".description"))
                .collect(Collectors.toMap(CSRImporterTranslatedProperty::getPropertyKey, CSRImporterTranslatedProperty::getDefaultFormat));
        Map<String, String> propertiesWithDescriptions = Arrays.stream(CSRImporterTranslatedProperty.values())
                .filter(property -> property.getKey().endsWith(".description"))
                .collect(Collectors.toMap(
                        property -> {
                            String propertyKey = property.getPropertyKey();
                            return propertyKey.substring(0, propertyKey.length() - ".description".length());
                        },
                        CSRImporterTranslatedProperty::getDefaultFormat));
        propertySpecs.stream()
                .peek(propertySpec -> assertThat(propertySpec.getDisplayName()).isEqualTo(propertiesWithTranslations.get(propertySpec.getName())))
                .forEach(propertySpec -> assertThat(propertySpec.getDescription()).isEqualTo(propertiesWithDescriptions.get(propertySpec.getName())));
    }

    @Test
    @Transactional
    public void testTimeoutDefaultValue() {
        assertThat(getPropertySpec(CSRImporterTranslatedProperty.TIMEOUT).getPossibleValues().getDefault())
                .isEqualTo(TimeDuration.seconds(30));
    }

    @Test
    @Transactional
    public void testNoSecurityAccessorValues() {
        assertThat(getPropertySpec(CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR).isReference()).isTrue();
        assertThat(getPropertySpec(CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR).isReference()).isTrue();
        assertAllValuesById(CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR); // no values
        assertAllValuesById(CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR); // no values
    }

    @Test
    @Transactional
    public void testSecurityAccessorValuesWithTrustedCertificate() throws Exception {
        TrustStore ts = securityManagementService.newTrustStore("Fixed")
                .add();
        TrustedCertificate certificate = ts.addCertificate("Trusted", SecurityTestUtils.generateSelfSignedCertificate("CN=Trusted").getFirst());
        KeyType subCA = securityManagementService.newTrustedCertificateType("SubCA")
                .add();
        SecurityAccessorType subCASecurityAccessor = securityManagementService.addSecurityAccessorType("SubCA", subCA)
                .managedCentrally()
                .purpose(SecurityAccessorType.Purpose.FILE_OPERATIONS)
                .trustStore(ts)
                .add();

        assertAllValuesById(CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR); // no values
        assertAllValuesById(CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR); // no values

        SecurityAccessor<CertificateWrapper> sa = securityManagementService.setDefaultValues(subCASecurityAccessor, certificate, null);

        assertAllValuesById(CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR, sa);
        assertAllValuesById(CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR); // no values
    }

    @Test
    @Transactional
    public void testSecurityAccessorValuesWithRequestableCertificate() throws Exception {
        TrustStore ts = securityManagementService.newTrustStore("Fixed")
                .add();
        CertificateWrapper certificate = securityManagementService.newCertificateWrapper("Cert");
        KeyType requestableType = securityManagementService.newCertificateType("Cert")
                .add();
        SecurityAccessorType securityAccessor = securityManagementService.addSecurityAccessorType("Cert", requestableType)
                .managedCentrally()
                .purpose(SecurityAccessorType.Purpose.FILE_OPERATIONS)
                .trustStore(ts)
                .add();
        SecurityAccessor<CertificateWrapper> sa = securityManagementService.setDefaultValues(securityAccessor, certificate, null);

        assertAllValuesById(CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR); // no values
        assertAllValuesById(CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR); // no values

        certificate.setCertificate(SecurityTestUtils.generateSelfSignedCertificate("CN=Some").getFirst(), Optional.empty());

        assertAllValuesById(CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR, sa);
        assertAllValuesById(CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR); // no values
    }

    @Test
    @Transactional
    public void testSecurityAccessorValuesWithClientCertificate() throws Exception {
        TrustStore ts = securityManagementService.newTrustStore("Fixed")
                .add();
        KeyType clientType = securityManagementService.newClientCertificateType("Cert", "SHA256withRSA")
                .RSA()
                .keySize(2048)
                .add();
        ClientCertificateWrapper certificate = securityManagementService.newClientCertificateWrapper(clientType, DataVaultSymmetricKeyFactory.KEY_ENCRYPTION_METHOD)
                .alias("Cert")
                .add();
        SecurityAccessorType securityAccessor = securityManagementService.addSecurityAccessorType("Cert", clientType)
                .managedCentrally()
                .purpose(SecurityAccessorType.Purpose.FILE_OPERATIONS)
                .trustStore(ts)
                .keyEncryptionMethod(DataVaultSymmetricKeyFactory.KEY_ENCRYPTION_METHOD)
                .add();
        SecurityAccessor<CertificateWrapper> sa = securityManagementService.setDefaultValues(securityAccessor, certificate, null);

        assertAllValuesById(CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR); // no values
        assertAllValuesById(CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR); // no values

        certificate.getPrivateKeyWrapper().generateValue();
        certificate.generateCSR(new X500NameBuilder().addRDN(BCStyle.CN, "Client").build());

        assertAllValuesById(CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR); // no values
        assertAllValuesById(CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR); // no values

        certificate.setCertificate(SecurityTestUtils.signCSR(certificate.getCSR().get(), "CN=Issuer", SecurityTestUtils.generateKeyPair().getPrivate()), Optional.empty());

        assertAllValuesById(CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR, sa);
        assertAllValuesById(CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR, sa);

        sa.delete();

        assertAllValuesById(CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR); // no values
        assertAllValuesById(CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR); // no values
    }

    @Test
    @Transactional
    public void testNoSecurityAccessorValuesWithUnsupportedTypeOfClientCertificate() throws Exception {
        TrustStore ts = securityManagementService.newTrustStore("Fixed")
                .add();
        KeyType clientType = securityManagementService.newClientCertificateType("Cert", "SHA256withECDSA")
                .ECDSA()
                .curve("secp256r1")
                .add();
        ClientCertificateWrapper certificate = securityManagementService.newClientCertificateWrapper(clientType, DataVaultSymmetricKeyFactory.KEY_ENCRYPTION_METHOD)
                .alias("Cert")
                .add();
        SecurityAccessorType securityAccessor = securityManagementService.addSecurityAccessorType("Cert", clientType)
                .managedCentrally()
                .purpose(SecurityAccessorType.Purpose.FILE_OPERATIONS)
                .trustStore(ts)
                .keyEncryptionMethod(DataVaultSymmetricKeyFactory.KEY_ENCRYPTION_METHOD)
                .add();
        securityManagementService.setDefaultValues(securityAccessor, certificate, null);

        certificate.getPrivateKeyWrapper().generateValue();
        certificate.generateCSR(new X500NameBuilder().addRDN(BCStyle.CN, "Client").build());
        certificate.setCertificate(SecurityTestUtils.signCSR(certificate.getCSR().get(), "CN=Issuer", SecurityTestUtils.generateKeyPair().getPrivate()), Optional.empty());

        assertAllValuesById(CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR); // no values
        assertAllValuesById(CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR); // no values
    }

    @Test
    @Transactional
    public void testNoSecurityAccessorValuesWithUnsupportedKeyLength() throws Exception {
        TrustStore ts = securityManagementService.newTrustStore("Fixed")
                .add();
        KeyType clientType = securityManagementService.newClientCertificateType("Cert", "SHA256withRSA")
                .RSA()
                .keySize(1024)
                .add();
        ClientCertificateWrapper certificate = securityManagementService.newClientCertificateWrapper(clientType, DataVaultSymmetricKeyFactory.KEY_ENCRYPTION_METHOD)
                .alias("Cert")
                .add();
        SecurityAccessorType securityAccessor = securityManagementService.addSecurityAccessorType("Cert", clientType)
                .managedCentrally()
                .purpose(SecurityAccessorType.Purpose.FILE_OPERATIONS)
                .trustStore(ts)
                .keyEncryptionMethod(DataVaultSymmetricKeyFactory.KEY_ENCRYPTION_METHOD)
                .add();
        securityManagementService.setDefaultValues(securityAccessor, certificate, null);

        certificate.getPrivateKeyWrapper().generateValue();
        certificate.generateCSR(new X500NameBuilder().addRDN(BCStyle.CN, "Client").build());
        certificate.setCertificate(SecurityTestUtils.signCSR(certificate.getCSR().get(), "CN=Issuer", SecurityTestUtils.generateKeyPair().getPrivate()), Optional.empty());

        assertAllValuesById(CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR); // no values
        assertAllValuesById(CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR); // no values
    }

    @Test
    @Transactional
    public void testNoSecurityAccessorValuesDueToInappropriatePurpose() throws Exception {
        TrustStore ts = securityManagementService.newTrustStore("Fixed")
                .add();
        KeyType clientType = securityManagementService.newClientCertificateType("Cert", "SHA256withRSA")
                .RSA()
                .keySize(2048)
                .add();
        ClientCertificateWrapper certificate = securityManagementService.newClientCertificateWrapper(clientType, DataVaultSymmetricKeyFactory.KEY_ENCRYPTION_METHOD)
                .alias("Cert")
                .add();
        SecurityAccessorType securityAccessor = securityManagementService.addSecurityAccessorType("Cert", clientType)
                .managedCentrally()
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS)
                .trustStore(ts)
                .keyEncryptionMethod(DataVaultSymmetricKeyFactory.KEY_ENCRYPTION_METHOD)
                .add();
        securityManagementService.setDefaultValues(securityAccessor, certificate, null);

        certificate.getPrivateKeyWrapper().generateValue();
        certificate.generateCSR(new X500NameBuilder().addRDN(BCStyle.CN, "Client").build());
        certificate.setCertificate(SecurityTestUtils.signCSR(certificate.getCSR().get(), "CN=Issuer", SecurityTestUtils.generateKeyPair().getPrivate()), Optional.empty());

        assertAllValuesById(CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR); // no values
        assertAllValuesById(CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR); // no values
    }

    @Test
    @Transactional
    public void testPropertiesAreValidWithNoExport() throws Exception {
        TrustStore ts = securityManagementService.newTrustStore("Fixed")
                .add();
        CertificateWrapper certificate = securityManagementService.newCertificateWrapper("Cert");
        KeyType requestableType = securityManagementService.newCertificateType("Cert")
                .add();
        SecurityAccessorType securityAccessor = securityManagementService.addSecurityAccessorType("Cert", requestableType)
                .managedCentrally()
                .purpose(SecurityAccessorType.Purpose.FILE_OPERATIONS)
                .trustStore(ts)
                .add();
        SecurityAccessor<CertificateWrapper> sa = securityManagementService.setDefaultValues(securityAccessor, certificate, null);
        certificate.setCertificate(SecurityTestUtils.generateSelfSignedCertificate("CN=Some").getFirst(), Optional.empty());

        pkiInMemoryPersistence.getFileImportService().newBuilder()
                .setName("Importer")
                .setPathMatcher("")
                .setImportDirectory(FileSystems.getDefault().getPath("import"))
                .setFailureDirectory(FileSystems.getDefault().getPath("failure"))
                .setSuccessDirectory(FileSystems.getDefault().getPath("success"))
                .setProcessingDirectory(FileSystems.getDefault().getPath("inProgress"))
                .setImporterName(CSRImporterFactory.NAME)
                .setActiveInUI(false)
                .setScheduleExpression(Never.NEVER)
                .addProperty(CSRImporterTranslatedProperty.TIMEOUT.getPropertyKey()).withValue(TimeDuration.minutes(1))
                .addProperty(CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR.getPropertyKey()).withValue(sa)
                .addProperty(CSRImporterTranslatedProperty.EXPORT_CERTIFICATES_SFTP.getPropertyKey()).withValue(false)
                .addProperty(CSRImporterTranslatedProperty.EXPORT_CERTIFICATES_FOLDER.getPropertyKey()).withValue(false)
                .addProperty(CSRImporterTranslatedProperty.CSR_MAPPING.getPropertyKey()).withValue("{}")
                .addProperty(CSRImporterTranslatedProperty.CA_NAME.getPropertyKey()).withValue("CA Name")
                .addProperty(CSRImporterTranslatedProperty.CA_PROFILE_NAME.getPropertyKey()).withValue("Profi")
                .addProperty(CSRImporterTranslatedProperty.CA_END_ENTITY_NAME.getPropertyKey()).withValue("Enti")
                .create();
    }

    @Test
    @Transactional
    public void testPropertiesAreValidWithExport() throws Exception {
        TrustStore ts = securityManagementService.newTrustStore("Fixed")
                .add();
        KeyType clientType = securityManagementService.newClientCertificateType("Cert", "SHA256withRSA")
                .RSA()
                .keySize(2048)
                .add();
        ClientCertificateWrapper certificate = securityManagementService.newClientCertificateWrapper(clientType, DataVaultSymmetricKeyFactory.KEY_ENCRYPTION_METHOD)
                .alias("Cert")
                .add();
        SecurityAccessorType securityAccessor = securityManagementService.addSecurityAccessorType("Cert", clientType)
                .managedCentrally()
                .purpose(SecurityAccessorType.Purpose.FILE_OPERATIONS)
                .trustStore(ts)
                .keyEncryptionMethod(DataVaultSymmetricKeyFactory.KEY_ENCRYPTION_METHOD)
                .add();
        SecurityAccessor<CertificateWrapper> sa = securityManagementService.setDefaultValues(securityAccessor, certificate, null);

        certificate.getPrivateKeyWrapper().generateValue();
        certificate.generateCSR(new X500NameBuilder().addRDN(BCStyle.CN, "Client").build());
        certificate.setCertificate(SecurityTestUtils.signCSR(certificate.getCSR().get(), "CN=Issuer", SecurityTestUtils.generateKeyPair().getPrivate()), Optional.empty());

        pkiInMemoryPersistence.getFileImportService().newBuilder()
                .setName("Importer")
                .setPathMatcher("")
                .setImportDirectory(FileSystems.getDefault().getPath("import"))
                .setFailureDirectory(FileSystems.getDefault().getPath("failure"))
                .setSuccessDirectory(FileSystems.getDefault().getPath("success"))
                .setProcessingDirectory(FileSystems.getDefault().getPath("inProgress"))
                .setImporterName(CSRImporterFactory.NAME)
                .setActiveInUI(false)
                .setScheduleExpression(Never.NEVER)
                .addProperty(CSRImporterTranslatedProperty.TIMEOUT.getPropertyKey()).withValue(TimeDuration.seconds(1))
                .addProperty(CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR.getPropertyKey()).withValue(sa)
                .addProperty(CSRImporterTranslatedProperty.EXPORT_CERTIFICATES_SFTP.getPropertyKey()).withValue(true)
                .addProperty(CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR.getPropertyKey()).withValue(sa)
                .addProperty(CSRImporterTranslatedProperty.EXPORT_CERTIFICATES_FOLDER.getPropertyKey()).withValue(true)
                .addProperty(CSRImporterTranslatedProperty.EXPORT_SFTP_HOSTNAME.getPropertyKey()).withValue("Hostname")
                .addProperty(CSRImporterTranslatedProperty.EXPORT_SFTP_PORT.getPropertyKey()).withValue(2222L)
                .addProperty(CSRImporterTranslatedProperty.EXPORT_SFTP_USER.getPropertyKey()).withValue("User")
                .addProperty(CSRImporterTranslatedProperty.EXPORT_SFTP_PASSWORD.getPropertyKey()).withValue("Password")
                .addProperty(CSRImporterTranslatedProperty.EXPORT_FILE_NAME.getPropertyKey()).withValue("Exported")
                .addProperty(CSRImporterTranslatedProperty.EXPORT_FILE_EXTENSION.getPropertyKey()).withValue("zip")
                .addProperty(CSRImporterTranslatedProperty.EXPORT_FILE_LOCATION.getPropertyKey()).withValue("Resource")
                .addProperty(CSRImporterTranslatedProperty.CA_NAME.getPropertyKey()).withValue("CA Name")
                .addProperty(CSRImporterTranslatedProperty.CA_PROFILE_NAME.getPropertyKey()).withValue("Profi")
                .addProperty(CSRImporterTranslatedProperty.CA_END_ENTITY_NAME.getPropertyKey()).withValue("Enti")
                .addProperty(CSRImporterTranslatedProperty.CSR_MAPPING.getPropertyKey()).withValue("{}")
                .create();
    }

    @Test
    @Transactional
    public void testZeroTimeout() {
        ImportScheduleBuilder builder = pkiInMemoryPersistence.getFileImportService().newBuilder()
                .setName("Importer")
                .setPathMatcher("")
                .setImportDirectory(FileSystems.getDefault().getPath("import"))
                .setFailureDirectory(FileSystems.getDefault().getPath("failure"))
                .setSuccessDirectory(FileSystems.getDefault().getPath("success"))
                .setProcessingDirectory(FileSystems.getDefault().getPath("inProgress"))
                .setImporterName(CSRImporterFactory.NAME)
                .setActiveInUI(false)
                .setScheduleExpression(Never.NEVER)
                .addProperty(CSRImporterTranslatedProperty.TIMEOUT.getPropertyKey()).withValue(TimeDuration.years(0))
                .addProperty(CSRImporterTranslatedProperty.EXPORT_CERTIFICATES_SFTP.getPropertyKey()).withValue(false);

        exceptionRule.expect(LocalizedFieldValidationException.class);
        exceptionRule.expectMessage("properties." + CSRImporterTranslatedProperty.TIMEOUT.getPropertyKey() + ": Positive value is required.");
        builder.create();
    }

    @Test
    @Transactional
    public void testNegativePort() {
        ImportScheduleBuilder builder = pkiInMemoryPersistence.getFileImportService().newBuilder()
                .setName("Importer")
                .setPathMatcher("")
                .setImportDirectory(FileSystems.getDefault().getPath("import"))
                .setFailureDirectory(FileSystems.getDefault().getPath("failure"))
                .setSuccessDirectory(FileSystems.getDefault().getPath("success"))
                .setProcessingDirectory(FileSystems.getDefault().getPath("inProgress"))
                .setImporterName(CSRImporterFactory.NAME)
                .setActiveInUI(false)
                .setScheduleExpression(Never.NEVER)
                .addProperty(CSRImporterTranslatedProperty.TIMEOUT.getPropertyKey()).withValue(TimeDuration.months(1))
                .addProperty(CSRImporterTranslatedProperty.EXPORT_SFTP_PORT.getPropertyKey()).withValue(-2L)
                .addProperty(CSRImporterTranslatedProperty.EXPORT_CERTIFICATES_SFTP.getPropertyKey()).withValue(false);

        exceptionRule.expect(LocalizedFieldValidationException.class);
        exceptionRule.expectMessage("properties." + CSRImporterTranslatedProperty.EXPORT_SFTP_PORT.getPropertyKey() + ": Positive value is required.");
        builder.create();
    }

    private static PropertySpec getPropertySpec(CSRImporterTranslatedProperty property) {
        Optional<PropertySpec> propertySpecOptional = csrImporterFactory.getPropertySpec(property.getPropertyKey());
        assertThat(propertySpecOptional).isPresent();
        return propertySpecOptional.get();
    }

    private static void assertAllValuesById(CSRImporterTranslatedProperty property, HasId... values) {
        PropertySpecPossibleValues actualValues = getPropertySpec(property).getPossibleValues();
        assertThat(actualValues.isExhaustive()).isTrue();
        assertThat(((List<?>) actualValues.getAllValues()).stream()
                .map(HasId.class::cast)
                .mapToLong(HasId::getId)
                .toArray())
                .containsOnly(Arrays.stream(values)
                        .mapToLong(HasId::getId)
                        .toArray());
    }
}
