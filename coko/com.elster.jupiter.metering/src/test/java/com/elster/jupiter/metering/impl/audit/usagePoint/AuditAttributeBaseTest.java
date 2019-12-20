/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.usagePoint;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.AuditOperationType;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.AuditTrail;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.impl.audit.usagePoint.attributes.GeneralInfoAttribute;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AuditAttributeBaseTest extends AuditUsagePointBaseTest {

    public void testCreateUsagePoint(Map<GeneralInfoAttribute, Object> attributesSet) {
        AuditService auditService = inMemoryBootstrapModule.getAuditService();
        String name = attributesSet.entrySet().stream()
                .filter(attribute -> attribute.getKey().equals(GeneralInfoAttribute.NAME))
                .findFirst()
                .map(attribute ->attribute.getValue().toString())
                .orElseGet(() -> "");
        UsagePoint usagePoint = createUsagePoint(name);
        checkNewUsagePoint(usagePoint, auditService);
        checkNewUsagePointValueSet(auditService, attributesSet);
    }

    public void testUpdateUsagePoint(UsagePoint usagePoint, Map<GeneralInfoAttribute, Object> from, Map<GeneralInfoAttribute, Object> to) {
        AuditService auditService = inMemoryBootstrapModule.getAuditService();
        Optional<AuditTrail> auditTrail =  getLastAuditTrail(auditService);
        assertThat(auditTrail).isPresent();
        assertAuditTrail(auditTrail.get(), usagePoint, getDomainContext(), getContextReference(), AuditOperationType.UPDATE);
        assertAuditTrailLog(auditService, from, to);
    }

    public UsagePoint createUsagePoint(String name){
        createUsagePoint(name, getServiceKind());
        UsagePoint usagePoint = findUsagePointByName(name).get();
        return usagePoint;
    }

    public void updateUsagePoint(UsagePoint usagePoint, Map<GeneralInfoAttribute, Object> to) {
        to.forEach((key, value) -> key.setValueToObject(usagePoint, value));
        usagePoint.update();
    }

    private void checkNewUsagePoint(UsagePoint usagePoint, AuditService auditService) {
        Optional<AuditTrail> auditTrail =  getLastAuditTrail(auditService);
        assertThat(auditTrail).isPresent();
        assertAuditTrail(auditTrail.get(), usagePoint, getDomainContext(), getContextReference(), AuditOperationType.INSERT);
    }

    private void checkNewUsagePointValueSet(AuditService auditService, Map<GeneralInfoAttribute, Object> to) {
        Optional<AuditTrail> auditTrail =  getLastAuditTrail(auditService);
        List<AuditLogChange> auditLogChanges = auditTrail.get().getLogs();
        to.forEach((key, value) -> {
            Optional<AuditLogChange> auditLogChange = auditLogChanges.stream()
                    .filter(log -> log.getName().compareToIgnoreCase(key.getName()) == 0)
                    .findFirst();

            assertThat(auditLogChange).isPresent();
            assertThat(auditLogChange.get().getValue()).isEqualTo(value);
        });
    }

    private void assertAuditTrailLog(AuditService auditService, Map<GeneralInfoAttribute, Object> from, Map<GeneralInfoAttribute, Object> to) {
        Optional<AuditTrail> auditTrail =  getLastAuditTrail(auditService);
        List<AuditLogChange> auditLogChanges = auditTrail.get().getLogs();
        int auditLogChangesSize = auditLogChanges.size();
        assertThat(auditLogChangesSize).isGreaterThanOrEqualTo(to.size());
        to.forEach((key, value) -> {
            Optional<AuditLogChange> auditLogChange = auditLogChanges.stream()
                    .filter(log -> log.getName().compareToIgnoreCase(key.getName()) == 0)
                    .findFirst();

            assertThat(auditLogChange).isPresent();
            assertThat(auditLogChange.get().getValue()).isEqualTo(value);
            Object fromValue = from.get(key);
            if (fromValue != null) {
                assertThat(auditLogChange.get().getPreviousValue()).isEqualTo(fromValue);
            }
        });
    }

    protected AuditDomainContextType getDomainContext(){
        return AuditDomainContextType.NODOMAIN;
    }

    protected ImmutableMap getContextReference(){
        return ImmutableMap.of();
    }
}
