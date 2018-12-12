/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceCommunicationFunction;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link DeviceCommunicationFunctionSetPersister} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-10 (14:08)
 */
public class DeviceCommunicationFunctionSetPersisterTest {

    @Test
    public void testToDbWithFullSet () {
        Set<DeviceCommunicationFunction> deviceCommunicationFunctions = EnumSet.allOf(DeviceCommunicationFunction.class);

        // Business method
        int persitentValue = new DeviceCommunicationFunctionSetPersister().toDb(deviceCommunicationFunctions);

        // Asserts
        assertThat(persitentValue).isEqualTo(31);
    }

    @Test
    public void testToDbWithEmptySet () {
        Set<DeviceCommunicationFunction> deviceCommunicationFunctions = EnumSet.noneOf(DeviceCommunicationFunction.class);

        // Business method
        int persitentValue = new DeviceCommunicationFunctionSetPersister().toDb(deviceCommunicationFunctions);

        // Asserts
        assertThat(persitentValue).isZero();
    }

    @Test
    public void testToDbWithOnlyConnection () {
        Set<DeviceCommunicationFunction> deviceCommunicationFunctions = EnumSet.of(DeviceCommunicationFunction.CONNECTION);

        // Business method
        int persitentValue = new DeviceCommunicationFunctionSetPersister().toDb(deviceCommunicationFunctions);

        // Asserts
        assertThat(persitentValue).isEqualTo(1);
    }

    @Test
    public void testToDbWithOnlyGateway () {
        Set<DeviceCommunicationFunction> deviceCommunicationFunctions = EnumSet.of(DeviceCommunicationFunction.GATEWAY);

        // Business method
        int persitentValue = new DeviceCommunicationFunctionSetPersister().toDb(deviceCommunicationFunctions);

        // Asserts
        assertThat(persitentValue).isEqualTo(2);
    }

    @Test
    public void testToDbWithOnlyProtocolSession () {
        Set<DeviceCommunicationFunction> deviceCommunicationFunctions = EnumSet.of(DeviceCommunicationFunction.PROTOCOL_SESSION);

        // Business method
        int persitentValue = new DeviceCommunicationFunctionSetPersister().toDb(deviceCommunicationFunctions);

        // Asserts
        assertThat(persitentValue).isEqualTo(4);
    }

    @Test
    public void testToDbWithOnlyProtocolMaster () {
        Set<DeviceCommunicationFunction> deviceCommunicationFunctions = EnumSet.of(DeviceCommunicationFunction.PROTOCOL_MASTER);

        // Business method
        int persitentValue = new DeviceCommunicationFunctionSetPersister().toDb(deviceCommunicationFunctions);

        // Asserts
        assertThat(persitentValue).isEqualTo(8);
    }

    @Test
    public void testToDbWithOnlyProtocolSlave () {
        Set<DeviceCommunicationFunction> deviceCommunicationFunctions = EnumSet.of(DeviceCommunicationFunction.PROTOCOL_SLAVE);

        // Business method
        int persitentValue = new DeviceCommunicationFunctionSetPersister().toDb(deviceCommunicationFunctions);

        // Asserts
        assertThat(persitentValue).isEqualTo(16);
    }

    @Test
    public void testFromDbForFullSet () {
        int persistentValue = 31;

        // Business method
        Set<DeviceCommunicationFunction> deviceCommunicationFunctions = new DeviceCommunicationFunctionSetPersister().fromDb(persistentValue);

        // Asserts
        assertThat(deviceCommunicationFunctions).containsAll(Arrays.asList(DeviceCommunicationFunction.values()));
    }

    @Test
    public void testFromDbForEmptySet () {
        int persistentValue = 0;

        // Business method
        Set<DeviceCommunicationFunction> deviceCommunicationFunctions = new DeviceCommunicationFunctionSetPersister().fromDb(persistentValue);

        // Asserts
        assertThat(deviceCommunicationFunctions).isEmpty();
    }

    @Test
    public void testFromDbForOnlyConnection () {
        int persistentValue = 1;

        // Business method
        Set<DeviceCommunicationFunction> deviceCommunicationFunctions = new DeviceCommunicationFunctionSetPersister().fromDb(persistentValue);

        // Asserts
        assertThat(deviceCommunicationFunctions).containsOnly(DeviceCommunicationFunction.CONNECTION);
    }

    @Test
    public void testFromDbForOnlyGateway () {
        int persistentValue = 2;

        // Business method
        Set<DeviceCommunicationFunction> deviceCommunicationFunctions = new DeviceCommunicationFunctionSetPersister().fromDb(persistentValue);

        // Asserts
        assertThat(deviceCommunicationFunctions).containsOnly(DeviceCommunicationFunction.GATEWAY);
    }

    @Test
    public void testFromDbForOnlyProtocolSession () {
        int persistentValue = 4;

        // Business method
        Set<DeviceCommunicationFunction> deviceCommunicationFunctions = new DeviceCommunicationFunctionSetPersister().fromDb(persistentValue);

        // Asserts
        assertThat(deviceCommunicationFunctions).containsOnly(DeviceCommunicationFunction.PROTOCOL_SESSION);
    }

    @Test
    public void testFromDbForOnlyProtocolMaster () {
        int persistentValue = 8;

        // Business method
        Set<DeviceCommunicationFunction> deviceCommunicationFunctions = new DeviceCommunicationFunctionSetPersister().fromDb(persistentValue);

        // Asserts
        assertThat(deviceCommunicationFunctions).containsOnly(DeviceCommunicationFunction.PROTOCOL_MASTER);
    }

    @Test
    public void testFromDbForOnlyProtocolSlave () {
        int persistentValue = 16;

        // Business method
        Set<DeviceCommunicationFunction> deviceCommunicationFunctions = new DeviceCommunicationFunctionSetPersister().fromDb(persistentValue);

        // Asserts
        assertThat(deviceCommunicationFunctions).containsOnly(DeviceCommunicationFunction.PROTOCOL_SLAVE);
    }

}