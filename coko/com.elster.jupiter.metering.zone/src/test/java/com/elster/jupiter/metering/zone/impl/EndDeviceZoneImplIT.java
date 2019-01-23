/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.zone.EndDeviceZone;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.Zone;
import com.elster.jupiter.metering.zone.ZoneType;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

@RunWith(MockitoJUnitRunner.class)
public class EndDeviceZoneImplIT extends BaseZoneIT {

    private static final String ZONE_NAME_A = "ZoneNameA";
    private static final String ZONE_NAME_B = "ZoneNameB";
    private static final String APPLICATION = "APPNAME";
    private static final String ZONE_TYPE_NAME_A = "ZoneTypeA";
    private static final String ZONE_TYPE_NAME_B = "ZoneTypeB";

    private EndDevice endDevice;
    private Zone zoneA;
    private Zone zoneB;

    @Before
    public void init() {
        meteringZoneService = injector.getInstance(MeteringZoneService.class);
        endDevice = createDevice();
        zoneA = createZone(meteringZoneService, ZONE_TYPE_NAME_A, ZONE_NAME_A);
        zoneB = createZone(meteringZoneService, ZONE_TYPE_NAME_B, ZONE_NAME_B);
    }

    @Test
    @Transactional
    public void testSave() {
        createEndDeviceZone(meteringZoneService, endDevice, zoneA);

        Finder<EndDeviceZone> finder = meteringZoneService.getByEndDevice(endDevice);
        assertThat(finder.stream().map(EndDeviceZone::getEndDevice).findFirst().get()).isEqualTo(endDevice);
        assertThat(finder.stream().map(EndDeviceZone::getZone).findFirst().get()).isEqualTo(zoneA);
    }

    @Test
    @Transactional
    public void testGetOrderByZoneTypeName() {
        createEndDeviceZone(meteringZoneService, endDevice, zoneB);
        createEndDeviceZone(meteringZoneService, endDevice, zoneA);

        Finder<EndDeviceZone> finder = meteringZoneService.getByEndDevice(endDevice);
        assertThat(finder.find()).hasSize(2);
        assertThat(finder.find().get(0).getEndDevice()).isEqualTo(endDevice);
        assertThat(finder.find().get(0).getZone()).isEqualTo(zoneA);
        assertThat(finder.find().get(1).getEndDevice()).isEqualTo(endDevice);
        assertThat(finder.find().get(1).getZone()).isEqualTo(zoneB);
    }

    @Test
    @Transactional
    public void testGetById() {
        createEndDeviceZone(meteringZoneService, endDevice, zoneA);

        Finder<EndDeviceZone> finder = meteringZoneService.getByEndDevice(endDevice);
        long endDeviceZoneId = finder.find().get(0).getId();

        EndDeviceZone endDeviceZone = meteringZoneService.getEndDeviceZone(endDeviceZoneId).get();
        assertThat(finder.find().get(0)).isEqualTo(endDeviceZone);
    }

    private void createEndDeviceZone(MeteringZoneService meteringZoneService, EndDevice endDevice, Zone zone) {
        meteringZoneService.newEndDeviceZoneBuilder()
                .withZone(zone)
                .withEndDevice(endDevice)
                .create();
    }

    @Test
    @Transactional
    public void testChangeZone() {
        createEndDeviceZone(meteringZoneService, endDevice, zoneA);

        Finder<EndDeviceZone> finder = meteringZoneService.getByEndDevice(endDevice);
        assertThat(finder.find()).hasSize(1);

        EndDeviceZone endDeviceZone = finder.find().get(0);
        assertThat(endDeviceZone.getEndDevice()).isEqualTo(endDevice);
        assertThat(endDeviceZone.getZone()).isEqualTo(zoneA);

        endDeviceZone.setZone(zoneB);
        endDeviceZone.save();

        finder = meteringZoneService.getByEndDevice(endDevice);
        assertThat(finder.find().get(0).getEndDevice()).isEqualTo(endDevice);
        assertThat(finder.find().get(0).getZone()).isEqualTo(zoneB);
    }

    @Test
    @Transactional
    public void testChangeZoneWithSameZoneType() {
        Optional<ZoneType> zoneTypeA = meteringZoneService.getZoneType(ZONE_TYPE_NAME_A, APPLICATION);
        assertThat(zoneTypeA.isPresent());
        Zone zoneB = createZone(meteringZoneService, zoneTypeA.get(), ZONE_NAME_B);
        createEndDeviceZone(meteringZoneService, endDevice, zoneA);

        Finder<EndDeviceZone> finder = meteringZoneService.getByEndDevice(endDevice);
        assertThat(finder.find()).hasSize(1);

        EndDeviceZone endDeviceZone = finder.find().get(0);
        assertThat(endDeviceZone.getEndDevice()).isEqualTo(endDevice);
        assertThat(endDeviceZone.getZone()).isEqualTo(zoneA);

        endDeviceZone.setZone(zoneB);
        endDeviceZone.save();

        finder = meteringZoneService.getByEndDevice(endDevice);
        assertThat(finder.find().get(0).getEndDevice()).isEqualTo(endDevice);
        assertThat(finder.find().get(0).getZone()).isEqualTo(zoneB);
    }

    @Test
    @Transactional
    public void testDeleteZone() {
        createEndDeviceZone(meteringZoneService, endDevice, zoneA);
        createEndDeviceZone(meteringZoneService, endDevice, zoneB);

        Finder<EndDeviceZone> finder = meteringZoneService.getByEndDevice(endDevice);
        assertThat(finder.find()).hasSize(2);
        finder.find().get(0).delete();
        finder = meteringZoneService.getByEndDevice(endDevice);
        assertThat(finder.find()).hasSize(1);
    }

    @Test
    @Transactional
    public void testDeleteZoneInUse() {
        createEndDeviceZone(meteringZoneService, endDevice, zoneA);
        try {
            zoneA.delete();
            failBecauseExceptionWasNotThrown(ZoneInUseLocalizedException.class);
        } catch (ZoneInUseLocalizedException e) {}
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "zone", messageId = "{" + MessageSeeds.Constants.ZONE_TYPE_NOT_UNIQUE + "}")
    public void testCreateEndDeviceZoneWithSameZoneType() {
        Optional<ZoneType> zoneTypeA = meteringZoneService.getZoneType(ZONE_TYPE_NAME_A, APPLICATION);
        assertThat(zoneTypeA.isPresent());
        Zone zoneB = createZone(meteringZoneService, zoneTypeA.get(), ZONE_NAME_B);

        createEndDeviceZone(meteringZoneService, endDevice, zoneA);
        createEndDeviceZone(meteringZoneService, endDevice, zoneB);
    }

    private ZoneType createZoneType(MeteringZoneService meteringZoneService, String zoneTypeName) {
        return meteringZoneService
                .newZoneTypeBuilder()
                .withName(zoneTypeName)
                .withApplication(APPLICATION)
                .create();
    }

    private Zone createZone(MeteringZoneService meteringZoneService, ZoneType zoneType, String zoneName) {
        return meteringZoneService.newZoneBuilder()
                .withName(zoneName)
                .withZoneType(zoneType)
                .create();
    }

    private Zone createZone(MeteringZoneService meteringZoneService, String zoneTypeName, String zoneName) {
        ZoneType zoneType = meteringZoneService
                .newZoneTypeBuilder()
                .withName(zoneTypeName)
                .withApplication(APPLICATION)
                .create();
        return meteringZoneService.newZoneBuilder()
                .withName(zoneName)
                .withZoneType(zoneType)
                .create();
    }

    private EndDevice createDevice() {
        return createDevice("amrId", "name");
    }

    private EndDevice createDevice(String id, String name) {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).orElseThrow(IllegalStateException::new);
        return amrSystem.createEndDevice(id, name);
    }
}
