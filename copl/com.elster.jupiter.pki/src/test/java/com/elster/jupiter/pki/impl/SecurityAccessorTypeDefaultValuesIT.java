/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.fileimport.impl.FileImportServiceImpl;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.impl.importers.csr.CSRImporterFactory;
import com.elster.jupiter.pki.impl.importers.csr.CSRImporterTranslatedProperty;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.pki.impl.wrappers.asymmetric.DataVaultPrivateKeyFactory;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.time.Never;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.security.KeyStore;
import java.security.Security;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;

public class SecurityAccessorTypeDefaultValuesIT {
    private static final String CAT_WITH_DEFAULT_VALUES_NAME = "CertificateAccessorTypeWithDefaultValues";
    private static final String CAT_NAME = "CertificateAccessorType";
    private static final String MDM_CERTIFICATE_NAME = "sm_2016_mdm_ca";
    private static final String ROOT_CERTIFICATE_NAME = "sm_2016_root_ca";
    private static PkiInMemoryPersistence inMemoryPersistence = new PkiInMemoryPersistence();
    private static SecurityManagementService securityManagementService;
    private static SecurityAccessorType cat, catWithDefaultValues;
    private static CertificateWrapper mdmCertificate, rootCertificate;

    @Rule
    public ExpectedException expectedRule = ExpectedException.none();
    @Rule
    public TestRule transactionalRule = new TransactionalRule(inMemoryPersistence.getTransactionService());

    @BeforeClass
    public static void initialize() throws Exception {
        inMemoryPersistence.activate();
        securityManagementService = inMemoryPersistence.getSecurityManagementService();
        ((SecurityManagementServiceImpl) securityManagementService).addPrivateKeyFactory(inMemoryPersistence.getDataVaultPrivateKeyFactory());
        ((SecurityManagementServiceImpl) securityManagementService).addSymmetricKeyFactory(inMemoryPersistence.getDataVaultSymmetricKeyFactory());
        ((SecurityManagementServiceImpl) securityManagementService).addPassphraseFactory(inMemoryPersistence.getDataVaultPassphraseFactory());
        ((FileImportServiceImpl) inMemoryPersistence.getFileImportService()).addFileImporter(inMemoryPersistence.getCSRImporterFactory());
        Security.addProvider(new BouncyCastleProvider());

        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        keyStore.load(SecurityAccessorTypeDefaultValuesIT.class.getResourceAsStream("SM2016MDMCA-chain.jks"), "changeit".toCharArray());
        try (TransactionContext context = inMemoryPersistence.getTransactionService().getContext()) {
            TrustStore trustStore = securityManagementService
                    .newTrustStore("imported")
                    .description("Imported from keystore")
                    .add();
            trustStore.loadKeyStore(keyStore);

            Optional<TrustStore> loaded = securityManagementService.findTrustStore("imported");
            assertThat(loaded).isPresent();
            trustStore = loaded.get();
            List<? extends CertificateWrapper> certificates = trustStore.getCertificates();
            assertThat(certificates.stream()
                    .map(CertificateWrapper::getAlias)
                    .collect(Collectors.toList()))
                    .containsOnly(MDM_CERTIFICATE_NAME, ROOT_CERTIFICATE_NAME);
            mdmCertificate = certificates.stream()
                    .filter(certificate -> MDM_CERTIFICATE_NAME.equals(certificate.getAlias()))
                    .findAny()
                    .orElseThrow(NoSuchElementException::new);
            rootCertificate = certificates.stream()
                    .filter(certificate -> ROOT_CERTIFICATE_NAME.equals(certificate.getAlias()))
                    .findAny()
                    .orElseThrow(NoSuchElementException::new);

            KeyType certs = securityManagementService.newCertificateType("Fiends")
                    .add();
            securityManagementService.addSecurityAccessorType(CAT_WITH_DEFAULT_VALUES_NAME, certs)
                    .managedCentrally()
                    .purpose(SecurityAccessorType.Purpose.FILE_OPERATIONS)
                    .description("just default certificates")
                    .trustStore(trustStore)
                    .add();
            cat = securityManagementService.addSecurityAccessorType(CAT_NAME, certs)
                    .purpose(SecurityAccessorType.Purpose.COMMUNICATION)
                    .description("just certificates")
                    .trustStore(trustStore)
                    .add();
            Optional<SecurityAccessorType> managedCentrally = securityManagementService.findSecurityAccessorTypeByName(CAT_WITH_DEFAULT_VALUES_NAME);
            assertThat(managedCentrally).isPresent();
            catWithDefaultValues = managedCentrally.get();
            assertThat(catWithDefaultValues.isManagedCentrally()).isTrue();
            context.commit();
        }
    }

    @AfterClass
    public static void uninstall() {
        ((SecurityManagementServiceImpl) securityManagementService).removePrivateKeyFactory(inMemoryPersistence.getDataVaultPrivateKeyFactory());
        ((SecurityManagementServiceImpl) securityManagementService).removeSymmetricKeyFactory(inMemoryPersistence.getDataVaultSymmetricKeyFactory());
        ((SecurityManagementServiceImpl) securityManagementService).removePassphraseFactory(inMemoryPersistence.getDataVaultPassphraseFactory());
        inMemoryPersistence.deactivate();
    }

    @Test
    @Transactional
    public void testGetDefaultValuesWithNoValues() {
        assertThat(securityManagementService.getDefaultValues(cat)).isEmpty();
        assertThat(securityManagementService.getDefaultValues(catWithDefaultValues)).isEmpty();
        assertThat(securityManagementService.getDefaultValues(cat, catWithDefaultValues)).isEmpty();
        assertThat(securityManagementService.getSecurityAccessors(SecurityAccessorType.Purpose.FILE_OPERATIONS)).isEmpty();
        assertThat(securityManagementService.getSecurityAccessors(SecurityAccessorType.Purpose.COMMUNICATION)).isEmpty();
    }

    @Test
    @Transactional
    public void testSetAndGetDefaultValues() {
        SecurityAccessor<CertificateWrapper> values = securityManagementService.setDefaultValues(catWithDefaultValues, rootCertificate, mdmCertificate);
        assertThat(values.getActualValue().map(CertificateWrapper::getAlias)).contains(ROOT_CERTIFICATE_NAME);
        assertThat(values.getTempValue().map(CertificateWrapper::getAlias)).contains(MDM_CERTIFICATE_NAME);

        Optional<SecurityAccessor<CertificateWrapper>> foundValuesOptional = securityManagementService.getDefaultValues(catWithDefaultValues)
                .map(sa -> (SecurityAccessor<CertificateWrapper>) sa);
        assertThat(foundValuesOptional).isPresent();
        values = foundValuesOptional.get();
        assertThat(values.getActualValue().map(CertificateWrapper::getAlias)).contains(ROOT_CERTIFICATE_NAME);
        assertThat(values.getTempValue().map(CertificateWrapper::getAlias)).contains(MDM_CERTIFICATE_NAME);

        assertThat(securityManagementService.getDefaultValues(cat)).isEmpty();
        assertThat(securityManagementService.getDefaultValues(cat, cat)).isEmpty();
        List<SecurityAccessor<CertificateWrapper>> foundValuesList = securityManagementService.getDefaultValues(cat, catWithDefaultValues).stream()
                .map(sa -> (SecurityAccessor<CertificateWrapper>) sa)
                .collect(Collectors.toList());
        assertThat(foundValuesList).hasSize(1);
        values = foundValuesList.get(0);
        assertThat(values.getKeyAccessorType().getName()).isEqualTo(CAT_WITH_DEFAULT_VALUES_NAME);
        assertThat(values.getActualValue().map(CertificateWrapper::getAlias)).contains(ROOT_CERTIFICATE_NAME);
        assertThat(values.getTempValue().map(CertificateWrapper::getAlias)).contains(MDM_CERTIFICATE_NAME);

        assertThat(securityManagementService.getSecurityAccessors(SecurityAccessorType.Purpose.COMMUNICATION)).isEmpty();
        foundValuesList = securityManagementService.getSecurityAccessors(SecurityAccessorType.Purpose.FILE_OPERATIONS).stream()
                .map(sa -> (SecurityAccessor<CertificateWrapper>) sa)
                .collect(Collectors.toList());
        assertThat(foundValuesList).hasSize(1);
        values = foundValuesList.get(0);
        assertThat(values.getKeyAccessorType().getName()).isEqualTo(CAT_WITH_DEFAULT_VALUES_NAME);
        assertThat(values.getActualValue().map(CertificateWrapper::getAlias)).contains(ROOT_CERTIFICATE_NAME);
        assertThat(values.getTempValue().map(CertificateWrapper::getAlias)).contains(MDM_CERTIFICATE_NAME);
    }

    @Test
    @Transactional
    public void testSetDefaultValuesForSecurityAccessorNotManagedCentrally() {
        expectedRule.expect(UnsupportedOperationException.class);
        expectedRule.expectMessage("Can't set default values for security accessor type that isn't managed centrally.");
        securityManagementService.setDefaultValues(cat, rootCertificate, mdmCertificate);
    }

    @Test
    @Transactional
    public void testSetDefaultValuesForSecurityAccessorOfWrongCryptographicType1() throws Exception {
        KeyType skType = securityManagementService.newSymmetricKeyType("Name", "AES", 128)
                .add();

        expectedRule.expect(UnsupportedOperationException.class);
        expectedRule.expectMessage("Default values are only supported for certificate accessor type.");
        securityManagementService.addSecurityAccessorType("Name", skType)
                .managedCentrally()
                .purpose(SecurityAccessorType.Purpose.COMMUNICATION)
                .description("just keys")
                .add();
    }

    @Test
    @Transactional
    public void testSetDefaultValuesForSecurityAccessorOfWrongCryptographicType2() throws Exception {
        KeyType skType = securityManagementService.newSymmetricKeyType("Name", "AES", 128)
                .add();
        SecurityAccessorType keyAccessorType = securityManagementService.addSecurityAccessorType("Name", skType)
                .description("just keys")
                .keyEncryptionMethod(DataVaultPrivateKeyFactory.KEY_ENCRYPTION_METHOD)
                .purpose(SecurityAccessorType.Purpose.COMMUNICATION)
                .duration(TimeDuration.years(1))
                .add();
        invoke(keyAccessorType, "setManagedCentrally", true);
        invoke(keyAccessorType, "save");

        expectedRule.expect(UnsupportedOperationException.class);
        expectedRule.expectMessage("Default values are only supported for certificate accessor type.");
        securityManagementService.setDefaultValues(keyAccessorType, rootCertificate, mdmCertificate);
    }

    @Test
    @Transactional
    public void testSetWrongDefaultValues() {
        KeyType skType = securityManagementService.newSymmetricKeyType("Name", "AES", 128)
                .add();
        SecurityAccessorType keyAccessorType = securityManagementService.addSecurityAccessorType("Name", skType)
                .description("just keys")
                .keyEncryptionMethod(DataVaultPrivateKeyFactory.KEY_ENCRYPTION_METHOD)
                .purpose(SecurityAccessorType.Purpose.COMMUNICATION)
                .duration(TimeDuration.years(1))
                .add();
        PlaintextSymmetricKey wrapper = (PlaintextSymmetricKey) securityManagementService.newSymmetricKeyWrapper(keyAccessorType);
        wrapper.generateValue();

        expectedRule.expect(IllegalArgumentException.class);
        expectedRule.expectMessage("Wrong type of actual or temp value; must be CertificateWrapper.");
        securityManagementService.setDefaultValues(catWithDefaultValues, wrapper, null);
    }

    @Test
    @Transactional
    public void testLockDefaultValuesWithNoValues() {
        assertThat(securityManagementService.lockDefaultValues(cat, 1)).isEmpty();
        assertThat(securityManagementService.lockDefaultValues(catWithDefaultValues, 1)).isEmpty();
    }

    @Test
    @Transactional
    public void testLockDefaultValues() {
        securityManagementService.setDefaultValues(catWithDefaultValues, rootCertificate, mdmCertificate);
        assertThat(securityManagementService.lockDefaultValues(cat, 1)).isEmpty();
        Optional<SecurityAccessor<CertificateWrapper>> foundValuesOptional = securityManagementService.lockDefaultValues(catWithDefaultValues, 1)
                .map(sa -> (SecurityAccessor<CertificateWrapper>) sa);
        assertThat(foundValuesOptional).isPresent();
        assertThat(foundValuesOptional.get().getActualValue().map(CertificateWrapper::getAlias)).contains(ROOT_CERTIFICATE_NAME);
        assertThat(foundValuesOptional.get().getTempValue().map(CertificateWrapper::getAlias)).contains(MDM_CERTIFICATE_NAME);
        assertThat(securityManagementService.lockDefaultValues(catWithDefaultValues, 2)).isEmpty();
    }

    @Test
    @Transactional
    public void testUpdateDefaultValues() {
        securityManagementService.setDefaultValues(catWithDefaultValues, mdmCertificate, null);
        Optional<SecurityAccessor<CertificateWrapper>> foundValuesOptional = securityManagementService.getDefaultValues(catWithDefaultValues)
                .map(sa -> (SecurityAccessor<CertificateWrapper>) sa);
        assertThat(foundValuesOptional).isPresent();
        SecurityAccessor<CertificateWrapper> values = foundValuesOptional.get();
        assertThat(values.getActualValue().map(CertificateWrapper::getAlias)).contains(MDM_CERTIFICATE_NAME);
        assertThat(values.getTempValue()).isEmpty();

        values.setActualValue(rootCertificate);
        values.setTempValue(mdmCertificate);
        values.save();

        foundValuesOptional = securityManagementService.getDefaultValues(catWithDefaultValues)
                .map(sa -> (SecurityAccessor<CertificateWrapper>) sa);
        assertThat(foundValuesOptional).isPresent();
        values = foundValuesOptional.get();
        assertThat(values.getActualValue().map(CertificateWrapper::getAlias)).contains(ROOT_CERTIFICATE_NAME);
        assertThat(values.getTempValue().map(CertificateWrapper::getAlias)).contains(MDM_CERTIFICATE_NAME);
    }

    @Test
    @Transactional
    public void testSwapValues() {
        securityManagementService.setDefaultValues(catWithDefaultValues, rootCertificate, mdmCertificate);
        Optional<SecurityAccessor<CertificateWrapper>> foundValuesOptional = securityManagementService.getDefaultValues(catWithDefaultValues)
                .map(sa -> (SecurityAccessor<CertificateWrapper>) sa);
        assertThat(foundValuesOptional).isPresent();
        SecurityAccessor<CertificateWrapper> values = foundValuesOptional.get();
        assertThat(values.getActualValue().map(CertificateWrapper::getAlias)).contains(ROOT_CERTIFICATE_NAME);
        assertThat(values.getTempValue().map(CertificateWrapper::getAlias)).contains(MDM_CERTIFICATE_NAME);
        assertThat(values.isSwapped()).isFalse();

        values.swapValues();

        foundValuesOptional = securityManagementService.getDefaultValues(catWithDefaultValues)
                .map(sa -> (SecurityAccessor<CertificateWrapper>) sa);
        assertThat(foundValuesOptional).isPresent();
        values = foundValuesOptional.get();
        assertThat(values.getActualValue().map(CertificateWrapper::getAlias)).contains(MDM_CERTIFICATE_NAME);
        assertThat(values.getTempValue().map(CertificateWrapper::getAlias)).contains(ROOT_CERTIFICATE_NAME);
        assertThat(values.isSwapped()).isTrue();
    }

    @Test
    @Transactional
    public void testClearTempCertificate() {
        securityManagementService.setDefaultValues(catWithDefaultValues, rootCertificate, mdmCertificate);
        Optional<SecurityAccessor<CertificateWrapper>> foundValuesOptional = securityManagementService.getDefaultValues(catWithDefaultValues)
                .map(sa -> (SecurityAccessor<CertificateWrapper>) sa);
        assertThat(foundValuesOptional).isPresent();
        SecurityAccessor<CertificateWrapper> values = foundValuesOptional.get();
        assertThat(values.getActualValue().map(CertificateWrapper::getAlias)).contains(ROOT_CERTIFICATE_NAME);
        assertThat(values.getTempValue().map(CertificateWrapper::getAlias)).contains(MDM_CERTIFICATE_NAME);

        values.clearTempValue();

        foundValuesOptional = securityManagementService.getDefaultValues(catWithDefaultValues)
                .map(sa -> (SecurityAccessor<CertificateWrapper>) sa);
        assertThat(foundValuesOptional).isPresent();
        values = foundValuesOptional.get();
        assertThat(values.getActualValue().map(CertificateWrapper::getAlias)).contains(ROOT_CERTIFICATE_NAME);
        assertThat(values.getTempValue()).isEmpty();
    }

    @Test
    @Transactional
    public void testDeleteAndResetDefaultValues() {
        securityManagementService.setDefaultValues(catWithDefaultValues, rootCertificate, mdmCertificate);
        Optional<SecurityAccessor<CertificateWrapper>> foundValuesOptional = securityManagementService.getDefaultValues(catWithDefaultValues)
                .map(sa -> (SecurityAccessor<CertificateWrapper>) sa);
        assertThat(foundValuesOptional).isPresent();
        assertThat(securityManagementService.lockDefaultValues(catWithDefaultValues, 1)).isPresent();

        foundValuesOptional.get().delete();

        assertThat(securityManagementService.getDefaultValues(catWithDefaultValues)).isEmpty();
        assertThat(securityManagementService.getDefaultValues(catWithDefaultValues, cat)).isEmpty();
        assertThat(securityManagementService.lockDefaultValues(catWithDefaultValues, 1)).isEmpty();
        assertThat(securityManagementService.lockDefaultValues(catWithDefaultValues, 2)).isEmpty();

        securityManagementService.setDefaultValues(catWithDefaultValues, mdmCertificate, null);
        foundValuesOptional = securityManagementService.getDefaultValues(catWithDefaultValues)
                .map(sa -> (SecurityAccessor<CertificateWrapper>) sa);
        assertThat(foundValuesOptional).isPresent();
        assertThat(securityManagementService.lockDefaultValues(catWithDefaultValues, 1)).isPresent();
        assertThat(foundValuesOptional.get().getActualValue().map(CertificateWrapper::getAlias)).contains(MDM_CERTIFICATE_NAME);
        assertThat(foundValuesOptional.get().getTempValue()).isEmpty();
    }

    @Test
    @Transactional
    public void testUsedByCertificateAccessors() {
        assertThat(securityManagementService.isUsedByCertificateAccessors(rootCertificate)).isFalse();
        assertThat(securityManagementService.isUsedByCertificateAccessors(mdmCertificate)).isFalse();

        securityManagementService.setDefaultValues(catWithDefaultValues, rootCertificate, null);
        assertThat(securityManagementService.isUsedByCertificateAccessors(rootCertificate)).isTrue();
        assertThat(securityManagementService.isUsedByCertificateAccessors(mdmCertificate)).isFalse();

        Optional<SecurityAccessor<CertificateWrapper>> foundValuesOptional = securityManagementService.getDefaultValues(catWithDefaultValues)
                .map(sa -> (SecurityAccessor<CertificateWrapper>) sa);
        assertThat(foundValuesOptional).isPresent();
        SecurityAccessor<CertificateWrapper> values = foundValuesOptional.get();
        values.setTempValue(mdmCertificate);
        values.save();

        assertThat(securityManagementService.isUsedByCertificateAccessors(rootCertificate)).isTrue();
        assertThat(securityManagementService.isUsedByCertificateAccessors(mdmCertificate)).isTrue();

        foundValuesOptional = securityManagementService.getDefaultValues(catWithDefaultValues)
                .map(sa -> (SecurityAccessor<CertificateWrapper>) sa);
        assertThat(foundValuesOptional).isPresent();
        foundValuesOptional.get().delete();

        assertThat(securityManagementService.isUsedByCertificateAccessors(rootCertificate)).isFalse();
        assertThat(securityManagementService.isUsedByCertificateAccessors(mdmCertificate)).isFalse();
    }

    @Test
    @Transactional
    public void testRemoveSecurityAccessorUsedByImporter() throws Exception {
        TrustStore trustStore = securityManagementService.newTrustStore("main")
                .add();
        KeyType certificateType = securityManagementService.newCertificateType("Cert")
                .add();
        CertificateWrapper certificateWrapper = securityManagementService.newCertificateWrapper("RSA-2048");
        certificateWrapper.setCertificate(TrustStoreImpl2IT.generateSelfSignedCertificate("CN=IAm").getFirst());
        SecurityAccessorType securityAccessorType = securityManagementService.addSecurityAccessorType("SA", certificateType)
                .purpose(SecurityAccessorType.Purpose.FILE_OPERATIONS)
                .managedCentrally()
                .trustStore(trustStore)
                .add();
        SecurityAccessor<CertificateWrapper> securityAccessor = securityManagementService.setDefaultValues(securityAccessorType, certificateWrapper, null);
        inMemoryPersistence.getFileImportService().newBuilder()
                .setName("main2Blocker")
                .setPathMatcher("")
                .setImportDirectory(FileSystems.getDefault().getPath("import"))
                .setFailureDirectory(FileSystems.getDefault().getPath("failure"))
                .setSuccessDirectory(FileSystems.getDefault().getPath("success"))
                .setProcessingDirectory(FileSystems.getDefault().getPath("inProgress"))
                .setImporterName(CSRImporterFactory.NAME)
                .setActiveInUI(false)
                .setScheduleExpression(Never.NEVER)
                .addProperty(CSRImporterTranslatedProperty.TIMEOUT.getPropertyKey()).withValue(TimeDuration.seconds(30))
                .addProperty(CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR.getPropertyKey()).withValue(securityAccessor)
                .addProperty(CSRImporterTranslatedProperty.EXPORT_CERTIFICATES.getPropertyKey()).withValue(false)
                .create();

        expectedRule.expect(PkiLocalizedException.class);
        expectedRule.expectMessage("The security accessor couldn't be removed because it is used on import services.");
        securityAccessor.delete();
    }

    /**
     * @param methodName should be unique!
     */
    private static void invoke(Object object, String methodName, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = Arrays.stream(object.getClass().getDeclaredMethods())
                .filter(m -> methodName.equals(m.getName()))
                .findAny()
                .orElseThrow(NoSuchMethodException::new);
        boolean accessible = method.isAccessible();
        if (!accessible) {
            method.setAccessible(true);
        }
        method.invoke(object, args);
        method.setAccessible(accessible);
    }
}
