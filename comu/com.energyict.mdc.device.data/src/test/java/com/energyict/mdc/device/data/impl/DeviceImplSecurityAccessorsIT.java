/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.impl.SecurityManagementServiceImpl;
import com.energyict.mdc.common.device.config.DeviceSecurityAccessorType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.KeyAccessorStatus;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.impl.pki.UnmanageableSecurityAccessorException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class DeviceImplSecurityAccessorsIT extends PersistenceIntegrationTest {
    private static final String DEVICE_NAME = "MyUniqueName";
    private static final String SERIAL_NUMBER = "MyUniqueSerialNumber";
    private static final String CERT_0_ALIAS = "Cert0";
    private static final String CERT_1_ALIAS = "Cert1";
    private static final String CERT_2_ALIAS = "Cert2";
    private static final String SA_CENTRALLY_MANAGED = "CentrallyManaged";
    private static final String SA_ON_DEVICE = "OnDevice";

    private Device device;
    private DeviceSecurityAccessorType certificateTypeOnDevice, certificateTypeManagedCentrally;
    private CertificateWrapper cert1, cert2;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private Device createSimpleDeviceWithName(String name) {
        return createSimpleDeviceWithName(name, inMemoryPersistence.getClock().instant());
    }

    private Device createSimpleDeviceWithName(String name, Instant start) {
        return inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, SERIAL_NUMBER, name, start);
    }

    @BeforeClass
    public static void setUpSecurityService() {
        SecurityManagementService securityManagementService = inMemoryPersistence.getSecurityManagementService();
        ((SecurityManagementServiceImpl) securityManagementService).addPrivateKeyFactory(inMemoryPersistence.getDataVaultPrivateKeyFactory());
        ((SecurityManagementServiceImpl) securityManagementService).addSymmetricKeyFactory(inMemoryPersistence.getDataVaultSymmetricKeyFactory());
        ((SecurityManagementServiceImpl) securityManagementService).addPassphraseFactory(inMemoryPersistence.getDataVaultPassphraseFactory());
        ((EventServiceImpl) inMemoryPersistence.getEventService()).addTopicHandler(inMemoryPersistence.getSecurityAccessorRemovalFromDeviceTypeEventHandler());
        Security.addProvider(new BouncyCastleProvider());
    }

    @AfterClass
    public static void tearDownSecurityService() {
        SecurityManagementService securityManagementService = inMemoryPersistence.getSecurityManagementService();
        ((SecurityManagementServiceImpl) securityManagementService).removePrivateKeyFactory(inMemoryPersistence.getDataVaultPrivateKeyFactory());
        ((SecurityManagementServiceImpl) securityManagementService).removeSymmetricKeyFactory(inMemoryPersistence.getDataVaultSymmetricKeyFactory());
        ((SecurityManagementServiceImpl) securityManagementService).removePassphraseFactory(inMemoryPersistence.getDataVaultPassphraseFactory());
    }

    @Before
    public void setUp() throws Exception {
        device = createSimpleDeviceWithName(DEVICE_NAME);
        SecurityManagementService securityManagementService = inMemoryPersistence.getSecurityManagementService();
        TrustStore trustStore = securityManagementService.newTrustStore("Store")
                .add();
        KeyType keyType = securityManagementService.newClientCertificateType("Some DSA key", "sha256withDSA")
                .DSA()
                .keySize(512)
                .add();
        CertificateWrapper cert0 = securityManagementService.newClientCertificateWrapper(keyType, "DataVault")
                .alias(CERT_0_ALIAS)
                .add();
        cert1 = securityManagementService.newClientCertificateWrapper(keyType, "DataVault")
                .alias(CERT_1_ALIAS)
                .add();
        cert2 = securityManagementService.newClientCertificateWrapper(keyType, "DataVault")
                .alias(CERT_2_ALIAS)
                .add();
        certificateTypeOnDevice = new DeviceSecurityAccessorType(Optional.empty(),securityManagementService.addSecurityAccessorType(SA_ON_DEVICE, keyType)
                .keyEncryptionMethod("DataVault")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS)
                .trustStore(trustStore)
                .add());
        certificateTypeManagedCentrally = new DeviceSecurityAccessorType(Optional.empty(),securityManagementService.addSecurityAccessorType(SA_CENTRALLY_MANAGED, keyType)
                .keyEncryptionMethod("DataVault")
                .purpose(SecurityAccessorType.Purpose.FILE_OPERATIONS)
                .trustStore(trustStore)
                .managedCentrally()
                .add());
        securityManagementService.setDefaultValues(certificateTypeManagedCentrally.getSecurityAccessor(), cert0, null);
    }

    @Test
    @Transactional
    public void testGetOnlyCentrallyManagedSecurityAccessors() {
        assertThat(device.getSecurityAccessors()).isEmpty();

        device.getDeviceType().addDeviceSecurityAccessorType(certificateTypeManagedCentrally, certificateTypeOnDevice);

        List<SecurityAccessor> securityAccessors = device.getSecurityAccessors();
        assertThat(securityAccessors).hasSize(1);
        SecurityAccessor<CertificateWrapper> securityAccessor = securityAccessors.get(0);
        assertThat(securityAccessor.getSecurityAccessorType().getId()).isEqualTo(certificateTypeManagedCentrally.getSecurityAccessor().getId());
        assertThat(securityAccessor.getDevice()).isEqualTo(device);
        assertThat(securityAccessor.getStatus()).isSameAs(KeyAccessorStatus.COMPLETE);
        assertThat(securityAccessor.isEditable()).isFalse();
        assertThat(securityAccessor.getActualValue().map(CertificateWrapper::getAlias)).contains(CERT_0_ALIAS);
        assertThat(securityAccessor.getTempValue()).isEmpty();
        assertThat(securityAccessor.getVersion()).isNegative();
    }

    @Test
    @Transactional
    public void testGetOnlyCentrallyManagedSecurityAccessor() {
        assertThat(device.getSecurityAccessor(certificateTypeOnDevice.getSecurityAccessor())).isEmpty();
        assertThat(device.getSecurityAccessor(certificateTypeManagedCentrally.getSecurityAccessor())).isEmpty();

        device.getDeviceType().addDeviceSecurityAccessorType(certificateTypeManagedCentrally, certificateTypeOnDevice);

        assertThat(device.getSecurityAccessor(certificateTypeOnDevice.getSecurityAccessor())).isEmpty();
        Optional<SecurityAccessor> securityAccessorOptional = device.getSecurityAccessor(certificateTypeManagedCentrally.getSecurityAccessor());
        assertThat(securityAccessorOptional).isPresent();
        SecurityAccessor<CertificateWrapper> securityAccessor = securityAccessorOptional.get();
        assertThat(securityAccessor.getSecurityAccessorType().getId()).isEqualTo(certificateTypeManagedCentrally.getSecurityAccessor().getId());
        assertThat(securityAccessor.getDevice()).isEqualTo(device);
        assertThat(securityAccessor.getStatus()).isSameAs(KeyAccessorStatus.COMPLETE);
        assertThat(securityAccessor.isEditable()).isFalse();
        assertThat(securityAccessor.getActualValue().map(CertificateWrapper::getAlias)).contains(CERT_0_ALIAS);
        assertThat(securityAccessor.getTempValue()).isEmpty();
        assertThat(securityAccessor.getVersion()).isNegative();
    }

    @Test
    @Transactional
    public void testBothSecurityAccessorsCentrallyManagedAndNot() {
        device.getDeviceType().addDeviceSecurityAccessorType(certificateTypeManagedCentrally, certificateTypeOnDevice);
        SecurityAccessor<CertificateWrapper> securityAccessor = device.newSecurityAccessor(certificateTypeOnDevice.getSecurityAccessor());
        securityAccessor.setTempValue(cert1);
        securityAccessor.save();
        assertThat(device.getSecurityAccessors()).hasSize(2);

        Optional<SecurityAccessor> securityAccessorOptional = device.getSecurityAccessor(certificateTypeManagedCentrally.getSecurityAccessor());
        assertThat(securityAccessorOptional).isPresent();
        securityAccessor = securityAccessorOptional.get();
        assertThat(securityAccessor.getSecurityAccessorType().getId()).isEqualTo(certificateTypeManagedCentrally.getSecurityAccessor().getId());
        assertThat(securityAccessor.getDevice()).isEqualTo(device);
        assertThat(securityAccessor.getStatus()).isSameAs(KeyAccessorStatus.COMPLETE);
        assertThat(securityAccessor.isEditable()).isFalse();
        assertThat(securityAccessor.getActualValue().map(CertificateWrapper::getAlias)).contains(CERT_0_ALIAS);
        assertThat(securityAccessor.getTempValue()).isEmpty();
        assertThat(securityAccessor.getVersion()).isNegative();

        securityAccessorOptional = device.getSecurityAccessor(certificateTypeOnDevice.getSecurityAccessor());
        assertThat(securityAccessorOptional).isPresent();
        securityAccessor = securityAccessorOptional.get();
        assertThat(securityAccessor.getSecurityAccessorType().getId()).isEqualTo(certificateTypeOnDevice.getSecurityAccessor().getId());
        assertThat(securityAccessor.getDevice()).isEqualTo(device);
        assertThat(securityAccessor.getStatus()).isSameAs(KeyAccessorStatus.INCOMPLETE);
        assertThat(securityAccessor.isEditable()).isTrue();
        assertThat(securityAccessor.getActualValue()).isEmpty();
        assertThat(securityAccessor.getTempValue().map(CertificateWrapper::getAlias)).contains(CERT_1_ALIAS);
        assertThat(securityAccessor.getVersion()).isEqualTo(2);

        device.removeSecurityAccessor(securityAccessor);
        assertThat(device.getSecurityAccessors().stream()
                .map(SecurityAccessor::getSecurityAccessorType)
                .collect(Collectors.toList()))
                .containsOnly(certificateTypeManagedCentrally.getSecurityAccessor());
        device.getDeviceType().removeDeviceSecurityAccessorType(certificateTypeOnDevice);
        assertThat(device.getDeviceType().getDeviceSecurityAccessorType()).containsOnly(certificateTypeManagedCentrally);
        device.getDeviceType().removeDeviceSecurityAccessorType(certificateTypeManagedCentrally);
        assertThat(device.getDeviceType().getDeviceSecurityAccessorType()).isEmpty();
        assertThat(device.getSecurityAccessors()).isEmpty();
    }

    @Test
    @Transactional
    public void testNotPossibleToRemoveSecurityAccessorTypeHavingSecurityAccessorFromDeviceType() {
        device.getDeviceType().addDeviceSecurityAccessorType(certificateTypeManagedCentrally, certificateTypeOnDevice);
        SecurityAccessor<CertificateWrapper> securityAccessor = device.newSecurityAccessor(certificateTypeOnDevice.getSecurityAccessor());
        securityAccessor.setTempValue(cert1);
        securityAccessor.save();
        assertThat(device.getSecurityAccessors().stream()
                .map(SecurityAccessor::getSecurityAccessorType)
                .collect(Collectors.toList()))
                .containsOnly(certificateTypeManagedCentrally.getSecurityAccessor(), certificateTypeOnDevice.getSecurityAccessor());

        device.getDeviceType().removeDeviceSecurityAccessorType(certificateTypeManagedCentrally);
        assertThat(device.getSecurityAccessors().stream()
                .map(SecurityAccessor::getSecurityAccessorType)
                .collect(Collectors.toList()))
                .containsOnly(certificateTypeOnDevice.getSecurityAccessor());

        expectedEx.expect(LocalizedException.class);
        expectedEx.expectMessage("The security accessor couldn't be removed from the device type"
                + " because keys/certificates are specified on devices of this device type.");
        device.getDeviceType().removeDeviceSecurityAccessorType(certificateTypeOnDevice);
    }

    @Test
    @Transactional
    public void testSecurityAccessorsOnDeviceAreManageable() {
        device.getDeviceType().addDeviceSecurityAccessorType(certificateTypeManagedCentrally, certificateTypeOnDevice);
        SecurityAccessor<CertificateWrapper> securityAccessor = device.newSecurityAccessor(certificateTypeOnDevice.getSecurityAccessor());
        securityAccessor.setTempValue(cert1);
        securityAccessor.save();

        Optional<SecurityAccessor> securityAccessorOptional = device.getSecurityAccessor(certificateTypeOnDevice.getSecurityAccessor());
        assertThat(securityAccessorOptional).isPresent();
        securityAccessor = securityAccessorOptional.get();
        securityAccessor.setActualValue(cert1);
        securityAccessor.setTempValue(cert2);
        securityAccessor.swapValues(); // saved inside
        securityAccessor.clearTempValue(); // saved inside

        securityAccessorOptional = device.getSecurityAccessor(certificateTypeOnDevice.getSecurityAccessor());
        assertThat(securityAccessorOptional).isPresent();
        securityAccessor = securityAccessorOptional.get();
        assertThat(securityAccessor.getSecurityAccessorType().getId()).isEqualTo(certificateTypeOnDevice.getSecurityAccessor().getId());
        assertThat(securityAccessor.getDevice()).isEqualTo(device);
        assertThat(securityAccessor.getStatus()).isSameAs(KeyAccessorStatus.COMPLETE);
        assertThat(securityAccessor.isEditable()).isTrue();
        assertThat(securityAccessor.getActualValue().map(CertificateWrapper::getAlias)).contains(CERT_2_ALIAS);
        assertThat(securityAccessor.getTempValue()).isEmpty();
        assertThat(securityAccessor.getVersion()).isEqualTo(4);

        securityAccessor.clearActualValue();
        device.removeSecurityAccessor(securityAccessor);
        assertThat(device.getSecurityAccessor(certificateTypeOnDevice.getSecurityAccessor())).isEmpty();
        assertThat(device.getSecurityAccessors()).hasSize(1);
    }

    @Test
    @Transactional
    public void testCentrallyManagedSecurityAccessorsAreNotManageableOnDevice1() {
        device.getDeviceType().addDeviceSecurityAccessorType(certificateTypeManagedCentrally, certificateTypeOnDevice);
        Optional<SecurityAccessor> securityAccessorOptional = device.getSecurityAccessor(certificateTypeManagedCentrally.getSecurityAccessor());
        assertThat(securityAccessorOptional).isPresent();
        SecurityAccessor<CertificateWrapper> securityAccessor = securityAccessorOptional.get();

        expectedEx.expect(UnmanageableSecurityAccessorException.class);
        expectedEx.expectMessage("It's not allowed to modify centrally managed security accessor '" + SA_CENTRALLY_MANAGED + "' on device level.");
        securityAccessor.clearTempValue();
    }

    @Test
    @Transactional
    public void testCentrallyManagedSecurityAccessorsAreNotManageableOnDevice2() {
        device.getDeviceType().addDeviceSecurityAccessorType(certificateTypeManagedCentrally, certificateTypeOnDevice);
        Optional<SecurityAccessor> securityAccessorOptional = device.getSecurityAccessor(certificateTypeManagedCentrally.getSecurityAccessor());
        assertThat(securityAccessorOptional).isPresent();
        SecurityAccessor<CertificateWrapper> securityAccessor = securityAccessorOptional.get();

        expectedEx.expect(UnmanageableSecurityAccessorException.class);
        expectedEx.expectMessage("It's not allowed to modify centrally managed security accessor '" + SA_CENTRALLY_MANAGED + "' on device level.");
        securityAccessor.setTempValue(cert1);
    }

    @Test
    @Transactional
    public void testCentrallyManagedSecurityAccessorsAreNotManageableOnDevice3() {
        device.getDeviceType().addDeviceSecurityAccessorType(certificateTypeManagedCentrally, certificateTypeOnDevice);
        Optional<SecurityAccessor> securityAccessorOptional = device.getSecurityAccessor(certificateTypeManagedCentrally.getSecurityAccessor());
        assertThat(securityAccessorOptional).isPresent();
        SecurityAccessor<CertificateWrapper> securityAccessor = securityAccessorOptional.get();

        expectedEx.expect(UnmanageableSecurityAccessorException.class);
        expectedEx.expectMessage("It's not allowed to modify centrally managed security accessor '" + SA_CENTRALLY_MANAGED + "' on device level.");
        securityAccessor.swapValues();
    }

    @Test
    @Transactional
    public void testCentrallyManagedSecurityAccessorsAreNotManageableOnDevice4() {
        device.getDeviceType().addDeviceSecurityAccessorType(certificateTypeManagedCentrally, certificateTypeOnDevice);
        Optional<SecurityAccessor> securityAccessorOptional = device.getSecurityAccessor(certificateTypeManagedCentrally.getSecurityAccessor());
        assertThat(securityAccessorOptional).isPresent();
        SecurityAccessor<CertificateWrapper> securityAccessor = securityAccessorOptional.get();

        expectedEx.expect(UnmanageableSecurityAccessorException.class);
        expectedEx.expectMessage("It's not allowed to modify centrally managed security accessor '" + SA_CENTRALLY_MANAGED + "' on device level.");
        securityAccessor.clearActualValue();
    }

    @Test
    @Transactional
    public void testCentrallyManagedSecurityAccessorsAreNotManageableOnDevice5() {
        device.getDeviceType().addDeviceSecurityAccessorType(certificateTypeManagedCentrally, certificateTypeOnDevice);
        Optional<SecurityAccessor> securityAccessorOptional = device.getSecurityAccessor(certificateTypeManagedCentrally.getSecurityAccessor());
        assertThat(securityAccessorOptional).isPresent();
        SecurityAccessor<CertificateWrapper> securityAccessor = securityAccessorOptional.get();

        expectedEx.expect(UnmanageableSecurityAccessorException.class);
        expectedEx.expectMessage("It's not allowed to modify centrally managed security accessor '" + SA_CENTRALLY_MANAGED + "' on device level.");
        securityAccessor.setActualValue(cert2);
    }

    @Test
    @Transactional
    public void testCentrallyManagedSecurityAccessorsAreNotManageableOnDevice6() {
        device.getDeviceType().addDeviceSecurityAccessorType(certificateTypeManagedCentrally, certificateTypeOnDevice);
        Optional<SecurityAccessor> securityAccessorOptional = device.getSecurityAccessor(certificateTypeManagedCentrally.getSecurityAccessor());
        assertThat(securityAccessorOptional).isPresent();
        SecurityAccessor<CertificateWrapper> securityAccessor = securityAccessorOptional.get();

        expectedEx.expect(UnmanageableSecurityAccessorException.class);
        expectedEx.expectMessage("It's not allowed to modify centrally managed security accessor '" + SA_CENTRALLY_MANAGED + "' on device level.");
        securityAccessor.save();
    }

    @Test
    @Transactional
    public void testCentrallyManagedSecurityAccessorsAreNotManageableOnDevice7() {
        device.getDeviceType().addDeviceSecurityAccessorType(certificateTypeManagedCentrally, certificateTypeOnDevice);
        Optional<SecurityAccessor> securityAccessorOptional = device.getSecurityAccessor(certificateTypeManagedCentrally.getSecurityAccessor());
        assertThat(securityAccessorOptional).isPresent();
        SecurityAccessor<CertificateWrapper> securityAccessor = securityAccessorOptional.get();

        expectedEx.expect(UnmanageableSecurityAccessorException.class);
        expectedEx.expectMessage("It's not allowed to modify centrally managed security accessor '" + SA_CENTRALLY_MANAGED + "' on device level.");
        securityAccessor.delete();
    }

    @Test
    @Transactional
    public void testCentrallyManagedSecurityAccessorsAreNotManageableOnDevice8() {
        device.getDeviceType().addDeviceSecurityAccessorType(certificateTypeManagedCentrally, certificateTypeOnDevice);
        Optional<SecurityAccessor> securityAccessorOptional = device.getSecurityAccessor(certificateTypeManagedCentrally.getSecurityAccessor());
        assertThat(securityAccessorOptional).isPresent();
        SecurityAccessor<CertificateWrapper> securityAccessor = securityAccessorOptional.get();

        expectedEx.expect(UnmanageableSecurityAccessorException.class);
        expectedEx.expectMessage("It's not allowed to modify centrally managed security accessor '" + SA_CENTRALLY_MANAGED + "' on device level.");
        securityAccessor.renew();
    }

    @Test
    @Transactional
    public void testCannotOverrideCentrallyManagedSecurityAccessorOnDevice() {
        device.getDeviceType().addDeviceSecurityAccessorType(certificateTypeManagedCentrally, certificateTypeOnDevice);

        expectedEx.expect(UnmanageableSecurityAccessorException.class);
        expectedEx.expectMessage("It's not allowed to modify centrally managed security accessor '" + SA_CENTRALLY_MANAGED + "' on device level.");
        device.newSecurityAccessor(certificateTypeManagedCentrally.getSecurityAccessor());
    }

    @Test
    @Transactional
    public void testCannotRemoveCentrallyManagedSecurityAccessorOnDevice() {
        device.getDeviceType().addDeviceSecurityAccessorType(certificateTypeManagedCentrally, certificateTypeOnDevice);
        Optional<SecurityAccessor> securityAccessorOptional = device.getSecurityAccessor(certificateTypeManagedCentrally.getSecurityAccessor());
        assertThat(securityAccessorOptional).isPresent();
        SecurityAccessor<CertificateWrapper> securityAccessor = securityAccessorOptional.get();

        expectedEx.expect(UnmanageableSecurityAccessorException.class);
        expectedEx.expectMessage("It's not allowed to modify centrally managed security accessor '" + SA_CENTRALLY_MANAGED + "' on device level.");
        device.removeSecurityAccessor(securityAccessor);
    }
}
