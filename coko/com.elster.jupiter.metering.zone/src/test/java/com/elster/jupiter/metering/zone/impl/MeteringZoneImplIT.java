/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.Zone;
import com.elster.jupiter.metering.zone.ZoneType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKeyProvider;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MeteringZoneImplIT extends BaseZoneIT {
    private static final String ZONE_NAME = "ZoneName";
    private static final String APPLICATION = "APPNAME";
    private static final String ZONE_TYPE_NAME = "ZoneTypeName";
    private ZoneType zoneType;

    @Before
    public void init() {
        super.init();
        zoneType = createZoneType(meteringZoneService);
    }

    @Test
    @Transactional
    public void testSave() {
        meteringZoneService.newZoneBuilder()
                .withName(ZONE_NAME)
                .withZoneType(zoneType)
                .create();

        Finder<Zone> finder = meteringZoneService.getZones(APPLICATION, meteringZoneService.newZoneFilter());

        assertThat(finder.stream().map(Zone::getName).findFirst().get()).isEqualTo(ZONE_NAME);
        assertThat(finder.stream().map(Zone::getApplication).findFirst().get()).isEqualTo(APPLICATION);
        assertThat(finder.stream().map(zone -> zone.getZoneType().getName()).findFirst().get()).isEqualTo(ZONE_TYPE_NAME);
    }

    @Test
    @Transactional
    public void testUpdate() {
        Zone zone = meteringZoneService.newZoneBuilder()
                .withName(ZONE_NAME)
                .withZoneType(zoneType)
                .create();
        zone.setName(ZONE_NAME + " modified");
        zone.save();
        assertThat(zone.getId()).isGreaterThan(0);
        assertThat(zone.getName()).isEqualTo(ZONE_NAME + " modified");
        assertThat(zone.getVersion()).isEqualTo(2);
        assertThat(zone.getUserName()).isNotEmpty();
        Instant now = Instant.now();
        assertThat(zone.getCreateTime()).isLessThanOrEqualTo(now);
        assertThat(zone.getModTime()).isLessThanOrEqualTo(now);
    }

    @Test
    @Transactional
    public void testDelete() {
        Zone zone = meteringZoneService.newZoneBuilder()
                .withName(ZONE_NAME)
                .withZoneType(zoneType)
                .create();
        zone.delete();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.ZONE_NAME_NOT_UNIQUE + "}")
    public void testSaveWithDuplicateName() {
        meteringZoneService.newZoneBuilder()
                .withName(ZONE_NAME)
                .withZoneType(zoneType)
                .create();
        meteringZoneService.newZoneBuilder()
                .withName(ZONE_NAME)
                .withZoneType(zoneType)
                .create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    public void testSaveWithEmptyName() {
        meteringZoneService.newZoneBuilder()
                .withName("")
                .withZoneType(zoneType)
                .create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    public void testSaveWithLargeName() {
        meteringZoneService.newZoneBuilder()
                .withName("123456789012345678901234567890123456789012345678901234567890123456789012345678901")
                .withZoneType(zoneType)
                .create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.ZONE_NAME_REQUIRED + "}")
    public void testSaveWithNULLName() {
        meteringZoneService.newZoneBuilder()
                .withName(null)
                .withZoneType(zoneType)
                .create();
    }

    @Test
    @Transactional
    public void testMeteringServiceProperties() {
        assertThat(MeteringZoneService.COMPONENTNAME).isEqualTo(((TranslationKeyProvider) meteringZoneService).getComponentName());
        assertThat(Layer.DOMAIN).isEqualTo(((TranslationKeyProvider) meteringZoneService).getLayer());
        assertThat(((TranslationKeyProvider) meteringZoneService).getKeys().isEmpty()).isFalse();
    }

    private ZoneType createZoneType(MeteringZoneService meteringZoneService) {
        return meteringZoneService
                .newZoneTypeBuilder()
                .withName(ZONE_TYPE_NAME)
                .withApplication(APPLICATION)
                .create();
    }
}
