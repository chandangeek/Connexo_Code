/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit;

import com.elster.jupiter.audit.AuditOperationType;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.AuditTrail;
import com.energyict.mdc.device.data.Device;

import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditDeviceIT extends AuditDeviceBase {

    private static final String DEVICE_NAME = "DeviceName";

    @Test
    public void createDeviceTest() {
        AuditService auditService = inMemoryPersistence.getAuditService();
        createDeviceWithName(DEVICE_NAME);
        Device device = findDeviceByName(DEVICE_NAME).get();
        checkNewDevice(device, auditService);
        removeDevice(device);
    }

    @Test
    public void removeDeviceTest() {
        AuditService auditService = inMemoryPersistence.getAuditService();
        createDeviceWithName(DEVICE_NAME);
        Device device = findDeviceByName(DEVICE_NAME).get();
        removeDevice(device);
        checkRemoveDeviceAttributeValueSet(device, auditService);
    }

    private void checkNewDevice(Device device, AuditService auditService) {
        Optional<AuditTrail> auditTrail =  getLastAuditTrail(auditService);
        assertThat(auditTrail).isPresent();
        assertAuditTrail(auditTrail.get(), device, AuditOperationType.INSERT);
    }

    private void checkRemoveDeviceAttributeValueSet(Device device, AuditService auditService) {
        Optional<AuditTrail> auditTrail =  getLastAuditTrail(auditService);
        assertThat(auditTrail).isPresent();
        assertAuditTrail(auditTrail.get(), device, AuditOperationType.DELETE);
        assertThat(auditTrail.get().getLogs()).hasSize(0);
    }



}
