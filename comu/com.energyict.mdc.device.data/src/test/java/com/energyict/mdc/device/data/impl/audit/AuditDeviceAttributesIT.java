/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.AuditOperationType;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.AuditTrail;
import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.device.data.Device;

import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditDeviceAttributesIT extends AuditDeviceBase {

    private static final String DEVICE_NAME = "DeviceName";
    private static final Map<DeviceAttribute, Object> deviceAttributesSet0 =  ImmutableMap.of(
            DeviceAttribute.DEVICE_NAME, DEVICE_NAME
    );
    private static final Map<DeviceAttribute, Object> deviceAttributesSet1 =  ImmutableMap.of(
            DeviceAttribute.SERIAL_NUMBER, "snSet1",
            DeviceAttribute.DEVICE_NAME, "dnSet1"
    );
    private static final Map<DeviceAttribute, Object> deviceAttributesSet2 =  ImmutableMap.of(
            DeviceAttribute.SERIAL_NUMBER, "snSet2",
            DeviceAttribute.DEVICE_NAME, "dnSet2",
            DeviceAttribute.MULTIPLIER, new BigDecimal(2)
    );

    @Test
    public void createDeviceTest() {
        AuditService auditService = inMemoryPersistence.getAuditService();
        createDeviceWithName(DEVICE_NAME);
        Device device = findDeviceByName(DEVICE_NAME).get();
        checkNewDeviceAttributeValueSet(device, auditService, deviceAttributesSet0);
        removeDevice(device);
    }

    @Test
    public void updateDeviceAttributesTest() {
        AuditService auditService = inMemoryPersistence.getAuditService();
        createDeviceWithName(DEVICE_NAME);
        Device device = findDeviceByName(DEVICE_NAME).get();
        try (TransactionContext context = getTransactionService().getContext()) {
            setAndCheckDeviceAttributeValueSet(device, auditService, deviceAttributesSet1, Optional.empty());
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            setAndCheckDeviceAttributeValueSet(device, auditService, deviceAttributesSet2, Optional.of(deviceAttributesSet1));
            context.commit();
        }
        removeDevice(device);
    }

    private void setAndCheckDeviceAttributeValueSet(Device device, AuditService auditService, Map<DeviceAttribute, Object> to, Optional<Map<DeviceAttribute, Object>> from) {
        to.forEach((key, value) -> key.setValueToObject(device, value));
        device.save();

        Optional<AuditTrail> auditTrail =  getLastAuditTrail(auditService);
        assertThat(auditTrail).isPresent();

        AuditTrail auditTrailValue =  auditTrail.get();
        assertThat(auditTrailValue.getOperation()).isEqualTo(AuditOperationType.UPDATE);
        assertThat(auditTrailValue.getTouchDomain().getName()).isEqualTo(device.getName());
        assertThat(auditTrailValue.getTouchDomain().getContextReference()).isEqualTo("");
        assertThat(auditTrailValue.getDomainContext()).isEqualTo(AuditDomainContextType.DEVICE_ATTRIBUTES);
        assertThat(auditTrailValue.getUser()).isEqualTo(inMemoryPersistence.getMockedUser().getName());
        assertThat(auditTrailValue.getPkDomain()).isEqualTo(device.getId());
        assertThat(auditTrailValue.getPkContext1()).isEqualTo(0);

        List<AuditLogChange> auditLogChanges = auditTrail.get().getLogs();
        assertThat(auditLogChanges).hasSize(to.size());
        to.forEach((key, value) -> {
            Optional<AuditLogChange> auditLogChange = auditLogChanges.stream()
                    .filter(log -> log.getName().compareToIgnoreCase(key.getName()) == 0)
                    .findFirst();

            assertThat(auditLogChange).isPresent();
            assertThat(auditLogChange.get().getValue()).isEqualTo(value);
            from.ifPresent(fromMap -> {
                Object fromValue = fromMap.get(key);
                if (fromValue != null) {
                    assertThat(auditLogChange.get().getPreviousValue()).isEqualTo(fromValue);
                }
            });
        });
    }

    private void checkNewDeviceAttributeValueSet(Device device, AuditService auditService, Map<DeviceAttribute, Object> to) {
        Optional<AuditTrail> auditTrail =  getLastAuditTrail(auditService);
        assertThat(auditTrail).isPresent();

        AuditTrail auditTrailValue =  auditTrail.get();
        assertThat(auditTrailValue.getOperation()).isEqualTo(AuditOperationType.INSERT);
        assertThat(auditTrailValue.getTouchDomain().getName()).isEqualTo(device.getName());
        assertThat(auditTrailValue.getTouchDomain().getContextReference()).isEqualTo("");
        assertThat(auditTrailValue.getDomainContext()).isEqualTo(AuditDomainContextType.DEVICE_ATTRIBUTES);
        assertThat(auditTrailValue.getUser()).isEqualTo(inMemoryPersistence.getMockedUser().getName());
        assertThat(auditTrailValue.getPkDomain()).isEqualTo(device.getId());
        assertThat(auditTrailValue.getPkContext1()).isEqualTo(0);

        List<AuditLogChange> auditLogChanges = auditTrail.get().getLogs();
        to.forEach((key, value) -> {
            Optional<AuditLogChange> auditLogChange = auditLogChanges.stream()
                    .filter(log -> log.getName().compareToIgnoreCase(key.getName()) == 0)
                    .findFirst();

            assertThat(auditLogChange).isPresent();
            assertThat(auditLogChange.get().getValue()).isEqualTo(value);
        });
    }

}
