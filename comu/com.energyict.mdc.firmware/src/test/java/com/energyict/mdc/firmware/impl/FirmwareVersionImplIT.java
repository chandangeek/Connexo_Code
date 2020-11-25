/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.util.collections.KPermutation;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class FirmwareVersionImplIT extends PersistenceTest {
    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;

    private DeviceType deviceType;

    @Before
    @Transactional
    public void setup() {
        when(this.deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
        deviceType = inMemoryPersistence.getInjector().getInstance(DeviceConfigurationService.class).newDeviceType("MyDeviceType", deviceProtocolPluggableClass);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + com.energyict.mdc.firmware.impl.MessageSeeds.Keys.NAME_MUST_BE_UNIQUE + "}")
    public void uniqueVersionTest() {
        String version = "Version1";
        inMemoryPersistence.getFirmwareService().newFirmwareVersion(deviceType, version, FirmwareStatus.GHOST, FirmwareType.METER, version).create();
        inMemoryPersistence.getFirmwareService().newFirmwareVersion(deviceType, version, FirmwareStatus.GHOST, FirmwareType.METER, version).create();
    }

    @Test
    @Transactional
    public void uniqueVersionCheckButDifferentTypeTest() {
        String version = "Version1";
        inMemoryPersistence.getFirmwareService().newFirmwareVersion(deviceType, version, FirmwareStatus.GHOST, FirmwareType.METER, version).create();
        inMemoryPersistence.getFirmwareService().newFirmwareVersion(deviceType, version, FirmwareStatus.GHOST, FirmwareType.COMMUNICATION, version).create();

        FirmwareService firmwareService = inMemoryPersistence.getFirmwareService();
        List<FirmwareVersion> firmwareVersions = inMemoryPersistence.getFirmwareService().findAllFirmwareVersions(firmwareService.filterForFirmwareVersion(deviceType)).find();
        assertThat(firmwareVersions).hasSize(2);
        assertThat(firmwareVersions.get(0).getFirmwareVersion()).isEqualTo(version);
        assertThat(firmwareVersions.get(1).getFirmwareVersion()).isEqualTo(version);
    }

    @Test
    @Transactional
    public void imageIdentifierTest() {
        FirmwareVersion meterVersion = inMemoryPersistence.getFirmwareService().newFirmwareVersion(deviceType, "Version1", FirmwareStatus.GHOST, FirmwareType.METER, "10.4.0").create();
        meterVersion.update();

        meterVersion = inMemoryPersistence.getFirmwareService().getFirmwareVersionById(meterVersion.getId()).get();
        assertThat(meterVersion.getImageIdentifier()).isEqualTo("10.4.0");

        meterVersion.setImageIdentifier("10.4.1");
        meterVersion.update();

        meterVersion = inMemoryPersistence.getFirmwareService().getFirmwareVersionById(meterVersion.getId()).get();
        assertThat(meterVersion.getImageIdentifier()).isEqualTo("10.4.1");
    }

    @Test
    @Transactional
    public void testFWWithDependencies() {
        FirmwareVersion[] versions = setUpFWWithDependencies();
        FirmwareService service = inMemoryPersistence.getFirmwareService();
        FirmwareVersion newMeterFW = versions[3];
        Optional<FirmwareVersion> newMeterFWOptional = service.getFirmwareVersionById(newMeterFW.getId());
        assertThat(newMeterFWOptional).contains(newMeterFW);

        newMeterFW = newMeterFWOptional.get();
        assertThat(newMeterFW.getAuxiliaryFirmwareDependency()).contains(versions[2]);
        assertThat(newMeterFW.getCommunicationFirmwareDependency()).contains(versions[1]);
        assertThat(newMeterFW.getMeterFirmwareDependency()).contains(versions[0]);
    }

    @Test
    @Transactional
    public void testRank() {
        FirmwareVersion[] versions = setUpFWWithDependencies();
        FirmwareVersion meterFW = versions[0];
        FirmwareVersion commFW = versions[1];
        FirmwareVersion auxFW = versions[2];
        FirmwareVersion newMeterFW = versions[3];
        FirmwareService service = inMemoryPersistence.getFirmwareService();

        assertThat(meterFW.getRank()).isEqualTo(1);
        assertThat(commFW.getRank()).isEqualTo(2);
        assertThat(auxFW.getRank()).isEqualTo(3);
        assertThat(newMeterFW.getRank()).isEqualTo(4);

        assertThat(meterFW.compareTo(commFW)).isLessThan(0);
        assertThat(commFW.compareTo(meterFW)).isGreaterThan(0);
        assertThat(commFW.compareTo(auxFW)).isLessThan(0);
        assertThat(auxFW.compareTo(commFW)).isGreaterThan(0);
        assertThat(meterFW.compareTo(newMeterFW)).isLessThan(0);
        assertThat(newMeterFW.compareTo(meterFW)).isGreaterThan(0);
        assertThat(commFW.compareTo(commFW)).isEqualTo(0);
        assertThat(auxFW.compareTo(auxFW)).isEqualTo(0);
        assertThat(meterFW.compareTo(meterFW)).isEqualTo(0);

        assertThat(service.getOrderedFirmwareVersions(deviceType)).containsExactly(newMeterFW, auxFW, commFW, meterFW);
    }

    @Test
    @Transactional
    public void testReorderFirmwares1() {
        FirmwareVersion[] versions = setUpFWWithDependencies();
        FirmwareVersion meterFW = versions[0];
        FirmwareVersion commFW = versions[1];
        FirmwareVersion auxFW = versions[2];
        FirmwareVersion newMeterFW = versions[3];
        FirmwareService service = inMemoryPersistence.getFirmwareService();

        service.reorderFirmwareVersions(deviceType, KPermutation.of(new long[]{4, 3, 2, 1}, new long[]{4, 1, 2, 3})); // newMeter, meter, comm, aux

        assertThat(service.getFirmwareVersionById(meterFW.getId()).map(FirmwareVersion::getRank)).contains(3);
        assertThat(service.getFirmwareVersionById(commFW.getId()).map(FirmwareVersion::getRank)).contains(2);
        assertThat(service.getFirmwareVersionById(auxFW.getId()).map(FirmwareVersion::getRank)).contains(1);
        assertThat(service.getFirmwareVersionById(newMeterFW.getId()).map(FirmwareVersion::getRank)).contains(4);
        assertThat(service.getOrderedFirmwareVersions(deviceType)).containsExactly(newMeterFW, meterFW, commFW, auxFW);
    }

    @Test
    @Transactional
    public void testReorderFirmwares2() {
        FirmwareVersion[] versions = setUpFWWithDependencies();
        FirmwareVersion meterFW = versions[0];
        FirmwareVersion commFW = versions[1];
        FirmwareVersion auxFW = versions[2];
        FirmwareVersion newMeterFW = versions[3];
        newMeterFW.setMeterFirmwareDependency(null);
        newMeterFW.setCommunicationFirmwareDependency(null);
        newMeterFW.setAuxiliaryFirmwareDependency(null);
        newMeterFW.update();
        FirmwareService service = inMemoryPersistence.getFirmwareService();

        service.reorderFirmwareVersions(deviceType, KPermutation.of(new long[]{4, 3, 2, 1}, new long[]{2, 1, 3, 4})); // comm, meter, aux, newMeter

        assertThat(service.getFirmwareVersionById(meterFW.getId()).map(FirmwareVersion::getRank)).contains(3);
        assertThat(service.getFirmwareVersionById(commFW.getId()).map(FirmwareVersion::getRank)).contains(4);
        assertThat(service.getFirmwareVersionById(auxFW.getId()).map(FirmwareVersion::getRank)).contains(2);
        assertThat(service.getFirmwareVersionById(newMeterFW.getId()).map(FirmwareVersion::getRank)).contains(1);
        assertThat(service.getOrderedFirmwareVersions(deviceType)).containsExactly(commFW, meterFW, auxFW, newMeterFW);
    }

    @Test
    @Transactional
    public void testUnsuccessfulReordering1() {
        setUpFWWithDependencies();

        expectedException.expect(LocalizedException.class);
        expectedException.expectMessage("Firmware 'm2' can't have dependency on minimal level meter firmware 'm1' with a higher rank.");

        inMemoryPersistence.getFirmwareService().reorderFirmwareVersions(deviceType, KPermutation.of(new long[]{4, 3, 2, 1}, new long[]{3, 2, 1, 4})); // aux, comm, meter, newMeter
    }

    @Test
    @Transactional
    public void testUnsuccessfulReordering2() {
        FirmwareVersion[] versions = setUpFWWithDependencies();
        FirmwareVersion newMeterFW = versions[3];
        newMeterFW.setMeterFirmwareDependency(null);
        newMeterFW.update();

        expectedException.expect(LocalizedException.class);
        expectedException.expectMessage("Firmware 'm2' can't have dependency on minimal communication firmware 'c1' with a higher rank.");

        inMemoryPersistence.getFirmwareService().reorderFirmwareVersions(deviceType, KPermutation.of(new long[]{4, 3, 2, 1}, new long[]{3, 2, 1, 4})); // comm, meter, newMeter
    }

    @Test
    @Transactional
    public void testUnsuccessfulReordering3() {
        setUpFWWithDependencies();

        expectedException.expect(LocalizedException.class);
        expectedException.expectMessage("The permutation of firmware versions isn't valid. Their list may have changed since the page was last updated.");

        inMemoryPersistence.getFirmwareService().reorderFirmwareVersions(deviceType, KPermutation.of(new long[]{4, 3, 2, 1}, new long[]{1, 2})); // meter, comm : obsolete list
    }

    @Test
    @Transactional
    public void testUnsuccessfulReordering4() {
        FirmwareVersion[] versions = setUpFWWithDependencies();
        FirmwareVersion meterFW = versions[0];
        FirmwareVersion commFW = versions[1];
        FirmwareVersion auxFW = versions[1];
        FirmwareVersion newMeterFW = versions[3];
        newMeterFW.setMeterFirmwareDependency(null);
        newMeterFW.setCommunicationFirmwareDependency(null);
        newMeterFW.setAuxiliaryFirmwareDependency(null);
        newMeterFW.update();
        commFW.setMeterFirmwareDependency(meterFW);
        commFW.update();

        expectedException.expect(LocalizedException.class);
        expectedException.expectMessage("Firmware 'c1' can't have dependency on minimal level meter firmware 'm1' with a higher rank.");

        inMemoryPersistence.getFirmwareService().reorderFirmwareVersions(deviceType, KPermutation.of(new long[]{4, 3, 2, 1}, new long[]{1, 2, 3, 4})); // meter, comm, newMeter
    }

    @Test
    @Transactional
    public void testFWWithWrongMeterDependency() {
        byte[] fwFile = "I'm a sad firmware".getBytes();
        FirmwareService service = inMemoryPersistence.getFirmwareService();
        FirmwareVersion commFW = service.newFirmwareVersion(deviceType, "c1", FirmwareStatus.TEST, FirmwareType.COMMUNICATION, "c10.4.0")
                .initFirmwareFile(fwFile)
                .create();

        expectedException.expect(ConstraintViolationException.class);
        expectedException.expectMessage(MessageSeeds.WRONG_FIRMWARE_TYPE_FOR_METER_FW_DEPENDENCY.getKey());

        service.newFirmwareVersion(deviceType, "m2", FirmwareStatus.TEST, FirmwareType.METER, "m10.4.1")
                .setMeterFirmwareDependency(commFW)
                .initFirmwareFile(fwFile)
                .create();
    }

    @Test
    @Transactional
    public void testFWWithWrongCommDependency() {
        byte[] fwFile = "I'm a sad firmware".getBytes();
        FirmwareService service = inMemoryPersistence.getFirmwareService();
        FirmwareVersion meterFW = service.newFirmwareVersion(deviceType, "m1", FirmwareStatus.TEST, FirmwareType.METER, "m10.4.0")
                .initFirmwareFile(fwFile)
                .create();

        expectedException.expect(ConstraintViolationException.class);
        expectedException.expectMessage(MessageSeeds.WRONG_FIRMWARE_TYPE_FOR_COM_FW_DEPENDENCY.getKey());

        service.newFirmwareVersion(deviceType, "m2", FirmwareStatus.TEST, FirmwareType.METER, "m10.4.1")
                .setCommunicationFirmwareDependency(meterFW)
                .initFirmwareFile(fwFile)
                .create();
    }

    @Test
    @Transactional
    public void testFWWithWrongAuxDependency() {
        byte[] fwFile = "I'm a sad firmware".getBytes();
        FirmwareService service = inMemoryPersistence.getFirmwareService();
        FirmwareVersion meterFW = service.newFirmwareVersion(deviceType, "m1", FirmwareStatus.TEST, FirmwareType.METER, "m10.4.0")
                .initFirmwareFile(fwFile)
                .create();

        expectedException.expect(ConstraintViolationException.class);
        expectedException.expectMessage(MessageSeeds.WRONG_FIRMWARE_TYPE_FOR_AUX_FW_DEPENDENCY.getKey());

        service.newFirmwareVersion(deviceType, "m2", FirmwareStatus.TEST, FirmwareType.METER, "m10.4.1")
                .setAuxiliaryFirmwareDependency(meterFW)
                .initFirmwareFile(fwFile)
                .create();
    }

    @Test
    @Transactional
    public void testEditFWWithDependencies() {
        FirmwareVersion[] versions = setUpFWWithDependencies();
        FirmwareService service = inMemoryPersistence.getFirmwareService();
        FirmwareVersion newMeterFW = versions[2];
        newMeterFW.setMeterFirmwareDependency(null);
        newMeterFW.setCommunicationFirmwareDependency(null);
        newMeterFW.setAuxiliaryFirmwareDependency(null);
        newMeterFW.update();

        Optional<FirmwareVersion> newMeterFWOptional = service.getFirmwareVersionById(newMeterFW.getId());
        assertThat(newMeterFWOptional).contains(newMeterFW);

        newMeterFW = newMeterFWOptional.get();
        assertThat(newMeterFW.getAuxiliaryFirmwareDependency()).isEmpty();
        assertThat(newMeterFW.getCommunicationFirmwareDependency()).isEmpty();
        assertThat(newMeterFW.getMeterFirmwareDependency()).isEmpty();
    }

    @Test
    @Transactional
    public void testMeterFWDependencyOfHigherRank() {
        FirmwareVersion[] versions = setUpFWWithDependencies();
        FirmwareVersion newMeterFW = versions[3];

        expectedException.expect(ConstraintViolationException.class);
        expectedException.expectMessage(MessageSeeds.WRONG_RANK_FOR_METER_FW_DEPENDENCY.getKey());

        versions[0].setMeterFirmwareDependency(newMeterFW);
        versions[0].update();
    }

    @Test
    @Transactional
    public void testCommFWDependencyOfHigherRank() {
        FirmwareVersion[] versions = setUpFWWithDependencies();
        FirmwareVersion commFW = versions[1];

        expectedException.expect(ConstraintViolationException.class);
        expectedException.expectMessage(MessageSeeds.WRONG_RANK_FOR_COM_FW_DEPENDENCY.getKey());

        versions[0].setCommunicationFirmwareDependency(commFW);
        versions[0].update();
    }

    @Test
    @Transactional
    public void testAuxFWDependencyOfHigherRank() {
        FirmwareVersion[] versions = setUpFWWithDependencies();
        FirmwareVersion auxFW = versions[2];

        expectedException.expect(ConstraintViolationException.class);
        expectedException.expectMessage(MessageSeeds.WRONG_RANK_FOR_AUX_FW_DEPENDENCY.getKey());

        versions[0].setAuxiliaryFirmwareDependency(auxFW);
        versions[0].update();
    }

    @Test
    @Transactional
    public void testAllKindsOfExceptions() {
        FirmwareVersion[] versions = setUpFWWithDependencies();
        FirmwareVersion commFW = versions[1];
        FirmwareVersion auxFW = versions[2];
        FirmwareVersion newMeterFW = versions[3];

        expectedException.expect(ConstraintViolationException.class);
        expectedException.expectMessage(MessageSeeds.WRONG_FIRMWARE_TYPE_FOR_METER_FW_DEPENDENCY.getKey());
        expectedException.expectMessage(MessageSeeds.WRONG_FIRMWARE_TYPE_FOR_COM_FW_DEPENDENCY.getKey());
        expectedException.expectMessage(MessageSeeds.WRONG_RANK_FOR_METER_FW_DEPENDENCY.getKey());
        expectedException.expectMessage(MessageSeeds.WRONG_RANK_FOR_COM_FW_DEPENDENCY.getKey());
        expectedException.expectMessage(MessageSeeds.WRONG_RANK_FOR_AUX_FW_DEPENDENCY.getKey());

        versions[0].setCommunicationFirmwareDependency(auxFW);
        versions[0].setMeterFirmwareDependency(commFW);
        versions[0].setAuxiliaryFirmwareDependency(newMeterFW);
        versions[0].update();
    }

    private FirmwareVersion[] setUpFWWithDependencies() {
        byte[] fwFile = "I'm a happy firmware".getBytes();
        FirmwareService service = inMemoryPersistence.getFirmwareService();
        FirmwareVersion meterFW = service.newFirmwareVersion(deviceType, "m1", FirmwareStatus.TEST, FirmwareType.METER, "m10.4.0")
                .initFirmwareFile(fwFile)
                .create();
        FirmwareVersion commFW = service.newFirmwareVersion(deviceType, "c1", FirmwareStatus.TEST, FirmwareType.COMMUNICATION, "c10.4.0")
                .initFirmwareFile(fwFile)
                .create();
        FirmwareVersion auxFW = service.newFirmwareVersion(deviceType, "a1", FirmwareStatus.TEST, FirmwareType.AUXILIARY, "a10.4.0")
                .initFirmwareFile(fwFile)
                .create();
        FirmwareVersion newMeterFW = service.newFirmwareVersion(deviceType, "m2", FirmwareStatus.TEST, FirmwareType.METER, "m10.4.1")
                .setMeterFirmwareDependency(meterFW)
                .setCommunicationFirmwareDependency(commFW)
                .setAuxiliaryFirmwareDependency(auxFW)
                .initFirmwareFile(fwFile)
                .create();
        return new FirmwareVersion[]{meterFW, commFW, auxFW, newMeterFW};
    }

}
