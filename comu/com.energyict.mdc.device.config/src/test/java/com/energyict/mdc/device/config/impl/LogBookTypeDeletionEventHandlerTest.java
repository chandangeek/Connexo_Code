/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.exceptions.VetoLogBookTypeDeletionBecauseStillUsedByDeviceTypesException;
import com.energyict.mdc.masterdata.LogBookType;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link LogBookTypeDeletionEventHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-14 (09:47)
 */
public class LogBookTypeDeletionEventHandlerTest extends DeviceTypeProvidingPersistenceTest {

    private static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.99.98.0.255");

    @Before
    public void registerEventHandlers () {
        inMemoryPersistence.registerEventHandlers();
    }

    @After
    public void unregisterEventHandlers () {
        inMemoryPersistence.unregisterEventHandlers();
    }

    @Test
    @Transactional
    public void testDeleteWhenNotInUse () {
        // Create brand new LogBookType
        LogBookType logBookType = inMemoryPersistence.getMasterDataService().newLogBookType("NotUsed", OBIS_CODE);
        logBookType.save();
        long id = logBookType.getId();

        // Business method
        logBookType.delete();

        // Asserts: should not get a veto exception
        Optional<LogBookType> shouldBeNull = inMemoryPersistence.getMasterDataService().findLogBookType(id);
        assertThat(shouldBeNull.isPresent()).isFalse();
    }

    @Test(expected = VetoLogBookTypeDeletionBecauseStillUsedByDeviceTypesException.class)
    @Transactional
    public void testDeleteWhenInUse () {
        // Create brand new LogBookType
        LogBookType logBookType = inMemoryPersistence.getMasterDataService().newLogBookType("InUse", OBIS_CODE);
        logBookType.save();

        // Add the LogBookType to our DeviceType
        deviceType.addLogBookType(logBookType);

        // Business method
        logBookType.delete();

        // Asserts: see expected exception rule
    }

}