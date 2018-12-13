/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import java.time.Instant;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BatchServiceTest extends PersistenceIntegrationTest {

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}", property = "batch")
    public void testBatchNameEmptyCheck() {
        inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "Device1", "", Instant.now());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "batch")
    public void testBatchNameTooLong() {
        inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "Device1", "111111111111111111111111111111111111111111111111111111111111111111111111111111111", Instant
                        .now());
    }

    @Test
    @Transactional
    public void testFindOrCreateBatch() {
        Batch batch1 = inMemoryPersistence.getBatchService().findOrCreateBatch("batch1");
        Batch batch2 = inMemoryPersistence.getBatchService().findOrCreateBatch("batch2");
        Batch batch1Copy = inMemoryPersistence.getBatchService().findOrCreateBatch("batch1");

        assertThat(batch1.getId()).isGreaterThan(0);
        assertThat(batch1.getName()).isEqualTo("batch1");
        assertThat(batch2.getId()).isGreaterThan(0);
        assertThat(batch2.getName()).isEqualTo("batch2");
        assertThat(batch1.getId()).isEqualTo(batch1Copy.getId());
    }

    @Test
    @Transactional
    public void testAddDeviceToBatch() {
        BatchService batchService = inMemoryPersistence.getBatchService();
        DeviceService deviceService = inMemoryPersistence.getDeviceService();

        Device device1 = deviceService.newDevice(deviceConfiguration, "Device1", "batch", Instant.now());
        Device device2 = deviceService.newDevice(deviceConfiguration, "Device2", "batch", Instant.now());
        Device device3 = deviceService.newDevice(deviceConfiguration, "Device3", "batch", Instant.now());

        Batch batch = batchService.findOrCreateBatch("batch");
        batch.addDevice(device1);
        Batch anotherBatch = batchService.findOrCreateBatch("anotherBatch");
        anotherBatch.addDevice(device3);

        batch = batchService.findOrCreateBatch("batch");
        anotherBatch = batchService.findOrCreateBatch("anotherBatch");

        assertThat(batch.isMember(device1)).isTrue();
        assertThat(batch.isMember(device2)).isTrue();
        assertThat(batch.isMember(device3)).isFalse();

        assertThat(anotherBatch.isMember(device1)).isFalse();
        assertThat(anotherBatch.isMember(device2)).isFalse();
        assertThat(anotherBatch.isMember(device3)).isTrue();
    }

    @Test
    @Transactional
    public void testRemoveDeviceFromBatch() {
        Batch batch = inMemoryPersistence.getBatchService().findOrCreateBatch("batch");
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "Device1", "Device1", Instant.now());
        device.save();

        batch.addDevice(device);
        assertThat(batch.isMember(device)).isTrue();
        assertThat(device.getBatch().get().getId()).isEqualTo(batch.getId());

        batch.removeDevice(device);
        assertThat(batch.isMember(device)).isFalse();
        assertThat(device.getBatch()).isEmpty();
    }
}
