package com.elster.jupiter.pki.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.fileimport.impl.FileImportServiceImpl;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.impl.importers.csr.CSRImporterFactory;
import com.elster.jupiter.pki.impl.importers.csr.CSRImporterTranslatedProperty;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.time.Never;

import java.nio.file.FileSystems;
import java.security.PrivateKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TrustStoreImpl2IT {

    private static PkiInMemoryPersistence inMemoryPersistence = new PkiInMemoryPersistence();

    @Rule
    public ExpectedException expectedRule = ExpectedException.none();
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
        ((SecurityManagementServiceImpl) inMemoryPersistence.getSecurityManagementService()).addPrivateKeyFactory(inMemoryPersistence.getDataVaultPrivateKeyFactory());
        ((SecurityManagementServiceImpl) inMemoryPersistence.getSecurityManagementService()).addSymmetricKeyFactory(inMemoryPersistence.getDataVaultSymmetricKeyFactory());
        ((SecurityManagementServiceImpl) inMemoryPersistence.getSecurityManagementService()).addPassphraseFactory(inMemoryPersistence.getDataVaultPassphraseFactory());
        ((FileImportServiceImpl) inMemoryPersistence.getFileImportService()).addFileImporter(inMemoryPersistence.getCSRImporterFactory());
    }

    @Before
    public void setUp() throws Exception {
        rootCertificate = SecurityTestUtils.generateSelfSignedCertificate("CN=ROOT");
        subCa1 = SecurityTestUtils.generateCertificate("CN=SubCA1", "CN=ROOT", rootCertificate.getLast());
        subCa2 = SecurityTestUtils.generateCertificate("CN=SubCA2", "CN=ROOT", rootCertificate.getLast());
        subCa3 = SecurityTestUtils.generateCertificate("CN=SubCA3", "CN=ROOT", rootCertificate.getLast());
        device = SecurityTestUtils.generateCertificate("CN=Device", "CN=SubCA2", subCa2.getLast());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        ((SecurityManagementServiceImpl) inMemoryPersistence.getSecurityManagementService()).removePrivateKeyFactory(inMemoryPersistence.getDataVaultPrivateKeyFactory());
        ((SecurityManagementServiceImpl) inMemoryPersistence.getSecurityManagementService()).removeSymmetricKeyFactory(inMemoryPersistence.getDataVaultSymmetricKeyFactory());
        ((SecurityManagementServiceImpl) inMemoryPersistence.getSecurityManagementService()).removePassphraseFactory(inMemoryPersistence.getDataVaultPassphraseFactory());
        ((FileImportServiceImpl) inMemoryPersistence.getFileImportService()).removeFileImporter(inMemoryPersistence.getCSRImporterFactory());
        inMemoryPersistence.deactivate();
    }

    @Test
    @Transactional
    public void testBasicValidation() throws Exception {
        TrustStore trustStore = inMemoryPersistence.getSecurityManagementService().newTrustStore("main1").add();
        trustStore.addCertificate("root", rootCertificate.getFirst());
        trustStore.addCertificate("SubCa1", subCa1.getFirst());
        trustStore.addCertificate("SubCa2", subCa2.getFirst());
        trustStore.addCertificate("SubCa3", subCa3.getFirst());

        // should be valid
        trustStore.validate(device.getFirst());
    }

    @Test
    @Transactional
    public void testValidationSelfSigned() throws Exception {
        TrustStore trustStore = inMemoryPersistence.getSecurityManagementService().newTrustStore("main3").add();
        trustStore.addCertificate("root", rootCertificate.getFirst());

        // should be valid
        trustStore.validate(rootCertificate.getFirst());
    }

    @Test
    @Transactional
    public void testValidationMissingSubCa() throws Exception {
        TrustStore trustStore = inMemoryPersistence.getSecurityManagementService().newTrustStore("main2").add();
        trustStore.addCertificate("root", rootCertificate.getFirst());
        trustStore.addCertificate("SubCa1", subCa1.getFirst());
        trustStore.addCertificate("SubCa2", subCa2.getFirst());
        trustStore.addCertificate("SubCa3", subCa3.getFirst());
        Pair<X509Certificate, PrivateKey> subCa4 = SecurityTestUtils.generateCertificate("CN=SubCA4", "CN=ROOT", rootCertificate.getLast());
        device = SecurityTestUtils.generateCertificate("CN=Device", "CN=SubCA4", subCa4.getLast());

        expectedRule.expect(CertPathValidatorException.class);
        trustStore.validate(device.getFirst());
    }

    @Test
    public void testCertificateValidateCertChain() throws Exception {
        X509Certificate root = rootCertificate.getFirst();
        X509Certificate subCa = subCa2.getFirst();
        X509Certificate deviceCert = device.getFirst();

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

    @Test
    @Transactional
    public void testRemoveUnusedTrustStore() {
        TrustStore trustStore = inMemoryPersistence.getSecurityManagementService().newTrustStore("main2").add();

        trustStore.delete();
        assertThat(inMemoryPersistence.getSecurityManagementService().findTrustStore("main2")).isEmpty();
    }

    @Test
    @Transactional
    public void testRemoveTrustStoreUsedBySecurityAccessor() {
        TrustStore trustStore = inMemoryPersistence.getSecurityManagementService().newTrustStore("main")
                .add();
        KeyType certificateType = inMemoryPersistence.getSecurityManagementService().newCertificateType("Cert")
                .add();
        inMemoryPersistence.getSecurityManagementService().addSecurityAccessorType("SA", certificateType)
                .purpose(SecurityAccessorType.Purpose.FILE_OPERATIONS)
                .managedCentrally()
                .trustStore(trustStore)
                .add();

        expectedRule.expect(VetoDeleteTrustStoreException.class);
        expectedRule.expectMessage("The trust store couldn't be removed because it is used on a security accessor.");
        trustStore.delete();
    }
}
