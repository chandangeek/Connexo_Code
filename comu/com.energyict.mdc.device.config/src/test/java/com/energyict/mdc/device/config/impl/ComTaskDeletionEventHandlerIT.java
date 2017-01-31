/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.tasks.ComTask;

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
public class ComTaskDeletionEventHandlerIT extends DeviceTypeProvidingPersistenceTest {
    private ProtocolDialectSharedData sharedData;

    @Before
    public void registerEventHandlers () {
        inMemoryPersistence.registerEventHandlers();
        sharedData = new ProtocolDialectSharedData();
    }

    @After
    public void unregisterEventHandlers () {
        inMemoryPersistence.unregisterEventHandlers();
        sharedData.invalidate();
    }

    @Test
    @Transactional
    public void testDeleteWhenNotInUse () {
        // Create brand new ComTask
        ComTask comTask = inMemoryPersistence.getTaskService().newComTask("NotUsed");
        comTask.createLogbooksTask().add();
        comTask.save();
        long id = comTask.getId();

        // Business method
        comTask.delete();

        // Asserts: should not get a veto exception
        Optional<ComTask> shouldBeNull = inMemoryPersistence.getTaskService().findComTask(id);
        assertThat(shouldBeNull.isPresent()).isFalse();
    }

    @Test(expected = VetoDeleteComTaskException.class)
    @Transactional
    public void testDeleteWhenInUse () {
        // Create brand new ComTask
        ComTask comTask = inMemoryPersistence.getTaskService().newComTask("NotUsed");
        comTask.createLogbooksTask().add();
        comTask.save();
        long id = comTask.getId();

        // Enable the ComTask in a newly created configuration
        DeviceConfiguration deviceConfiguration = this.deviceType.newConfiguration("testDeleteWhenInUse").add();
        ProtocolDialectConfigurationProperties properties = deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(sharedData.getProtocolDialect());
        SecurityPropertySet securityPropertySet =
                deviceConfiguration
                        .createSecurityPropertySet("testDeleteWhenInUse")
                        .authenticationLevel(0)
                        .encryptionLevel(0)
                        .build();
        deviceConfiguration.enableComTask(comTask, securityPropertySet, properties).add();
        deviceConfiguration.save();


        try {
            // Business method
            comTask.delete();
        }
        catch (VetoDeleteComTaskException e) {
            // Asserts: see expected exception rule
            assertThat(inMemoryPersistence.getTaskService().findComTask(id).isPresent()).isTrue();
            throw e;
        }
    }

}