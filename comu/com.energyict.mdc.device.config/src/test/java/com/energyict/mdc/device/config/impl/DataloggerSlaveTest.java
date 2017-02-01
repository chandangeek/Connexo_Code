/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.DeviceTypePurpose;
import com.energyict.mdc.device.config.exceptions.DataloggerSlaveException;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.masterdata.LogBookType;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DataloggerSlaveTest extends DeviceTypeProvidingPersistenceTest {

    @Test
    @Transactional
    public void createDataloggerSlaveWithoutViolations() {
        String deviceTypeName = "createDataloggerSlaveWithoutViolations";
        DeviceType deviceType;
        // Business method
        DeviceType.DeviceTypeBuilder deviceTypeBuilder = inMemoryPersistence.getDeviceConfigurationService()
                .newDataloggerSlaveDeviceTypeBuilder(deviceTypeName, getDefaultDeviceLifeCycle());
        String description = "For testing purposes only";
        deviceTypeBuilder.setDescription(description);
        deviceType = deviceTypeBuilder.create();

        // Asserts
        assertThat(deviceType).isNotNull();
        assertThat(deviceType.getId()).isGreaterThan(0);
        assertThat(deviceType.getName()).isEqualTo(deviceTypeName);
        assertThat(deviceType.getLogBookTypes()).isEmpty();
        assertThat(deviceType.getLoadProfileTypes()).isEmpty();
        assertThat(deviceType.getRegisterTypes()).isEmpty();
        assertThat(deviceType.getDeviceProtocolPluggableClass().isPresent()).isFalse();
        assertThat(deviceType.getDescription()).isEqualTo(description);
        assertThat(deviceType.isDataloggerSlave()).isTrue();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "deviceLifeCycle")
    public void createDataloggerSlaveWithoutDeviceLifeCycleTest() {
        String deviceTypeName = "createDataloggerSlaveWithoutViolations";
        DeviceType deviceType;
        // Business method
        DeviceType.DeviceTypeBuilder deviceTypeBuilder = inMemoryPersistence.getDeviceConfigurationService()
                .newDataloggerSlaveDeviceTypeBuilder(deviceTypeName, null);
        String description = "For testing purposes only";
        deviceTypeBuilder.setDescription(description);
        deviceType = deviceTypeBuilder.create();
    }

    @Test
    @Transactional
    public void canChangeFromDataloggerSlaveToRegularWhenNoConfigsTest() {
        String deviceTypeName = "canChangeFromDataloggerSlaveToRegularWhenNoConfigsTest";
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService()
                .newDataloggerSlaveDeviceTypeBuilder(deviceTypeName, getDefaultDeviceLifeCycle())
                .create();

        assertThat(deviceType.isDataloggerSlave()).isTrue();

        deviceType.setDeviceTypePurpose(DeviceTypePurpose.REGULAR);
        deviceType.setDeviceProtocolPluggableClass(deviceProtocolPluggableClass);
        deviceType.update();

        DeviceType reloadedDeviceType = reloadDeviceType(deviceType);
        assertThat(reloadedDeviceType.isDataloggerSlave()).isFalse();
    }

    @Test
    @Transactional
    public void canChangeFromRegularDeviceTypeToDataloggerSlaveWhenNoConfigsTest() {
        String deviceTypeName = "canChangeFromRegularDeviceTypeToDataloggerSlaveWhenNoConfigsTest";
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService()
                .newDeviceTypeBuilder(deviceTypeName, deviceProtocolPluggableClass, getDefaultDeviceLifeCycle())
                .create();

        assertThat(deviceType.isDataloggerSlave()).isFalse();

        deviceType.setDeviceTypePurpose(DeviceTypePurpose.DATALOGGER_SLAVE);
        deviceType.update();

        DeviceType reloadedDeviceType = reloadDeviceType(deviceType);
        assertThat(reloadedDeviceType.isDataloggerSlave()).isTrue();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CANNOT_CHANGE_DEVICE_TYPE_PURPOSE_WHEN_CONFIGS + "}", property = "deviceTypePurpose", strict = false)
    public void cannotChangeFromDataloggerSlaveToRegularWhenConfigsTest() {
        String deviceTypeName = "cannotChangeFromDataloggerSlaveToRegularWhenConfigsTest";
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService()
                .newDataloggerSlaveDeviceTypeBuilder(deviceTypeName, getDefaultDeviceLifeCycle())
                .create();

        deviceType.newConfiguration("Default").add();

        deviceType.setDeviceTypePurpose(DeviceTypePurpose.REGULAR);
        deviceType.setDeviceProtocolPluggableClass(deviceProtocolPluggableClass);
        deviceType.update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CANNOT_CHANGE_DEVICE_TYPE_PURPOSE_WHEN_CONFIGS + "}", property = "deviceTypePurpose")
    public void cannotChangeFromRegularToDataloggerSlaveWhenConfigsTest() {
        String deviceTypeName = "cannotChangeFromRegularToDataloggerSlaveWhenConfigsTest";
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService()
                .newDeviceTypeBuilder(deviceTypeName, deviceProtocolPluggableClass, getDefaultDeviceLifeCycle())
                .create();

        deviceType.newConfiguration("Default").add();

        deviceType.setDeviceTypePurpose(DeviceTypePurpose.DATALOGGER_SLAVE);
        deviceType.update();
    }

    @Test(expected = DataloggerSlaveException.class)
    @Transactional
    public void cannotChangeDeviceTypePurposeToDataloggerSlaveWhenAlreadyHaveConfigsWithLogBookSpecsTest() {
        LogBookType logBookType = createLogBookType();
        String deviceTypeName = "cannotChangeDeviceTypePurposeToDataloggerSlaveWhen";
        DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService()
                .newDeviceTypeBuilder(deviceTypeName, deviceProtocolPluggableClass, getDefaultDeviceLifeCycle())
                .withLogBookTypes(Collections.singletonList(logBookType))
                .create();

        assertThat(deviceType.isDataloggerSlave()).isFalse();

        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("Test");
        deviceConfigurationBuilder.newLogBookSpec(logBookType);
        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();

        try {
            deviceType.setDeviceTypePurpose(DeviceTypePurpose.DATALOGGER_SLAVE);
            deviceType.update();
        } catch (DataloggerSlaveException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.CANNOT_CHANGE_DEVICE_TYPE_PURPOSE_TO_DATALOGGER_SLAVE_WHEN_LOGBOOK_SPECS_EXIST);
            throw e;
        }
    }

    @Test(expected = DataloggerSlaveException.class)
    @Transactional
    public void cannotAddLogBookTypesToADataloggerSlaveDeviceTypeTest() {
        LogBookType logBookType = createLogBookType();
        String deviceTypeName = "cannotAddLogBookTypesToADataloggerSlaveDeviceTypeTest";
        try {
            DeviceType deviceType = inMemoryPersistence.getDeviceConfigurationService()
                    .newDataloggerSlaveDeviceTypeBuilder(deviceTypeName, getDefaultDeviceLifeCycle())
                    .withLogBookTypes(Collections.singletonList(logBookType))
                    .create();
        } catch (DataloggerSlaveException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.DATALOGGER_SLAVE_NO_LOGBOOKTYPE_SUPPORT);
            throw e;
        }
    }

    private LogBookType createLogBookType() {
        LogBookType logBookType = inMemoryPersistence.getMasterDataService()
                .newLogBookType("LBT1", ObisCode.fromString("0.0.99.98.0.255"));
        logBookType.save();
        return logBookType;
    }

    private DeviceLifeCycle getDefaultDeviceLifeCycle() {
        return inMemoryPersistence
                .getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
    }

    private DeviceType reloadDeviceType(DeviceType deviceType) {
        return inMemoryPersistence.getDeviceConfigurationService()
                .findDeviceType(deviceType.getId())
                .get();
    }

}