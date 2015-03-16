package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;

import java.util.Optional;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link LogBookTypeDeletionEventHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-14 (09:47)
 */
public class ComTaskDeletionEventHandlerIT extends DeviceTypeProvidingPersistenceTest {

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
        deviceType.save();
        SecurityPropertySet securityPropertySet =
                deviceConfiguration
                        .createSecurityPropertySet("testDeleteWhenInUse")
                        .authenticationLevel(0)
                        .encryptionLevel(0)
                        .build();
        deviceConfiguration.enableComTask(comTask, securityPropertySet).add();
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