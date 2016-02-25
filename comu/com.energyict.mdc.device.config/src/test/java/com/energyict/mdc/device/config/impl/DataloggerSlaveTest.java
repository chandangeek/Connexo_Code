package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.State;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Copyrights EnergyICT
 * Date: 24.02.16
 * Time: 14:13
 */
@RunWith(MockitoJUnitRunner.class)
public class DataloggerSlaveTest extends DeviceTypeProvidingPersistenceTest {

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DATALOGGER_SLAVE_LIFECYCLE_WITH_COMMUNICATION + "}", property = "deviceLifeCycle")
    public void createDataloggerSlaveWithCommunicationRelatedDeviceLifeCycleTest() {
        String deviceTypeName = "createDataloggerSlaveWithCommunicationRelatedDeviceLifeCycleTest";
        DeviceType deviceType;
        // Business method
        deviceType = inMemoryPersistence.getDeviceConfigurationService()
                .newDataloggerSlaveDeviceType(deviceTypeName, getDefaultDeviceLifeCycle());
        String description = "For testing purposes only";
        deviceType.setDescription(description);
        deviceType.save();
    }

    private DeviceLifeCycle getDefaultDeviceLifeCycle() {
        return inMemoryPersistence
                .getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
    }

    @Test
    @Transactional
    public void createDataloggerSlaveWithoutViolations() {
        String deviceTypeName = "createDataloggerSlaveWithoutViolations";
        DeviceType deviceType;
        // Business method
        deviceType = inMemoryPersistence.getDeviceConfigurationService()
                .newDataloggerSlaveDeviceType(deviceTypeName, createNoCommunicationRelatedDeviceLifeCycle());
        String description = "For testing purposes only";
        deviceType.setDescription(description);
        deviceType.save();

        // Asserts
        assertThat(deviceType).isNotNull();
        assertThat(deviceType.getId()).isGreaterThan(0);
        assertThat(deviceType.getName()).isEqualTo(deviceTypeName);
        assertThat(deviceType.getLogBookTypes()).isEmpty();
        assertThat(deviceType.getLoadProfileTypes()).isEmpty();
        assertThat(deviceType.getRegisterTypes()).isEmpty();
        assertThat(deviceType.getDeviceProtocolPluggableClass()).isNull();
        assertThat(deviceType.getDescription()).isEqualTo(description);
        assertThat(deviceType.isDataloggerSlave()).isTrue();
    }

    private DeviceLifeCycle createNoCommunicationRelatedDeviceLifeCycle() {
        FiniteStateMachineBuilder nothingBuilder = inMemoryPersistence.getFiniteStateMachineService()
                .newFiniteStateMachine("Nothing");
        State removed = nothingBuilder.newStandardState(DefaultState.REMOVED.getKey()).complete();

        DeviceLifeCycle noComs = inMemoryPersistence.getDeviceLifeCycleConfigurationService()
                .newDeviceLifeCycleUsing("NoComs", nothingBuilder.complete(removed))
                .complete();
        noComs.save();
        return noComs;
    }

}