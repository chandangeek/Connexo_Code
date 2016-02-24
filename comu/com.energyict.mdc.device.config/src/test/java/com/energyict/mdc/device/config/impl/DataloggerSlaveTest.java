package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.device.config.DeviceType;

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
    public void createDataloggerSlaveTest() {
        String deviceTypeName = "createDataloggerSlaveTest";
        DeviceType deviceType;
        // Business method
        deviceType = inMemoryPersistence.getDeviceConfigurationService()
                .newDataloggerSlaveDeviceType(deviceTypeName, inMemoryPersistence
                        .getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get());
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

}