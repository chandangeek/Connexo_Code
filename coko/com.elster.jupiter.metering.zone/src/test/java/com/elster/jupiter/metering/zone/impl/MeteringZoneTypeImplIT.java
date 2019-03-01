/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.ZoneType;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MeteringZoneTypeImplIT extends BaseZoneIT {
    private static final String APPLICATION_1 = "APPNAME1";
    private static final String APPLICATION_2 = "APPNAME2";
    private static final String ZONE_TYPE_NAME_1 = "ZoneTypeName1";
    private static final String ZONE_TYPE_NAME_2 = "ZoneTypeName2";

    @Test
    @Transactional
    public void testSave() {
        createZoneType(meteringZoneService, ZONE_TYPE_NAME_1, APPLICATION_1);
        createZoneType(meteringZoneService, ZONE_TYPE_NAME_2, APPLICATION_1);

        List<ZoneType> zoneTypes = meteringZoneService.getZoneTypes(APPLICATION_1);
        assertThat(zoneTypes).hasSize(2);
        assertThat(zoneTypes.get(0).getName()).isEqualTo(ZONE_TYPE_NAME_1);
        assertThat(zoneTypes.get(1).getName()).isEqualTo(ZONE_TYPE_NAME_2);
    }

    @Test
    @Transactional
    public void testGetOrderByName() {
        createZoneType(meteringZoneService, ZONE_TYPE_NAME_2, APPLICATION_1);
        createZoneType(meteringZoneService, ZONE_TYPE_NAME_1, APPLICATION_1);

        List<ZoneType> zoneTypes = meteringZoneService.getZoneTypes(APPLICATION_1);
        assertThat(zoneTypes).hasSize(2);
        assertThat(zoneTypes.get(0).getName()).isEqualTo(ZONE_TYPE_NAME_1);
        assertThat(zoneTypes.get(1).getName()).isEqualTo(ZONE_TYPE_NAME_2);
    }

    @Test
    @Transactional
    public void testGetFilterByAppName() {
        createZoneType(meteringZoneService, ZONE_TYPE_NAME_1, APPLICATION_1);
        createZoneType(meteringZoneService, ZONE_TYPE_NAME_2, APPLICATION_2);

        List<ZoneType> zoneTypes = meteringZoneService.getZoneTypes(APPLICATION_1);
        assertThat(zoneTypes).hasSize(1);
        assertThat(zoneTypes.get(0).getName()).isEqualTo(ZONE_TYPE_NAME_1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.ZONE_TYPE_NAME_NOT_UNIQUE + "}")
    public void testSaveDuplicate() {
        createZoneType(meteringZoneService, ZONE_TYPE_NAME_1, APPLICATION_1);
        createZoneType(meteringZoneService, ZONE_TYPE_NAME_1, APPLICATION_1);
    }

    @Test
    @Transactional
    public void testSaveSameZoneType() {
        ZoneType zoneType = createZoneType(meteringZoneService, ZONE_TYPE_NAME_1, APPLICATION_1);
        zoneType.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "typeName", messageId = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    public void testSaveWithEmptyName() {
        createZoneType(meteringZoneService, "", APPLICATION_1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "typeName", messageId = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    public void testSaveWithLargeName() {
        createZoneType(meteringZoneService, "123456789012345678901234567890123456789012345678901234567890123456789012345678901", APPLICATION_1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "typeName", messageId = "{" + MessageSeeds.Constants.ZONE_TYPE_NAME_REQUIRED + "}")
    public void testSaveWithNULLName() {
        createZoneType(meteringZoneService, null, APPLICATION_1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "application", messageId = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_10 + "}")
    public void testSaveWithEmptyApplication() {
        createZoneType(meteringZoneService, ZONE_TYPE_NAME_1, "");
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "application", messageId = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_10 + "}")
    public void testSaveWithLargeApplication() {
        createZoneType(meteringZoneService, ZONE_TYPE_NAME_1, "01234567890");
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "application", messageId = "{" + MessageSeeds.Constants.ZONE_TYPE_APP_REQUIRED + "}")
    public void testSaveWithNULLApplication() {
        createZoneType(meteringZoneService, ZONE_TYPE_NAME_1, null);
    }

    private ZoneType createZoneType(MeteringZoneService meteringZoneService, String name, String application) {
        return meteringZoneService
                .newZoneTypeBuilder()
                .withName(name)
                .withApplication(application)
                .create();
    }
}
