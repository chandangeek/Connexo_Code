/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.usagePoint;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.AuditOperationType;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.AuditTrail;
import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.impl.audit.usagePoint.attributes.TechInfoForElectricityAttribute;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.YesNoAnswer;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditTechInfoForElectricityIT extends AuditAttributeBase {

    private static final String USAGEPOINT_NAME_2 = "UPName2";
    private static final ServiceKind SERVICE_KIND = ServiceKind.ELECTRICITY;

    private static final Map<TechInfoForElectricityAttribute, Object> upAttributesSet0 =  ImmutableMap.of(
            TechInfoForElectricityAttribute.COLLAR, YesNoAnswer.UNKNOWN,
            TechInfoForElectricityAttribute.GROUNDED, YesNoAnswer.UNKNOWN,
            TechInfoForElectricityAttribute.INTERRUPTIBLE, YesNoAnswer.UNKNOWN,
            TechInfoForElectricityAttribute.LIMITER, YesNoAnswer.UNKNOWN
    );
    private static final Map<TechInfoForElectricityAttribute, Object> upAttributesSet1 =  ImmutableMap.of(
            TechInfoForElectricityAttribute.COLLAR, YesNoAnswer.YES,
            TechInfoForElectricityAttribute.LIMITER, YesNoAnswer.NO,
            TechInfoForElectricityAttribute.LOAD_LIMITER_TYPE, 1,
            TechInfoForElectricityAttribute.PHASE_CODE, PhaseCode.ABCN
    );
    private static final Map<TechInfoForElectricityAttribute, Object> upAttributesSet2 =  ImmutableMap.of(
            TechInfoForElectricityAttribute.COLLAR, YesNoAnswer.NO,
            TechInfoForElectricityAttribute.LIMITER, YesNoAnswer.YES,
            TechInfoForElectricityAttribute.LOAD_LIMITER_TYPE, 2,
            TechInfoForElectricityAttribute.PHASE_CODE, PhaseCode.ABC
    );

    @Test
    public void updateUsagePointTest() {
        UsagePoint usagePoint;
        try (TransactionContext context = getTransactionService().getContext()) {
            usagePoint = createUsagePoint(USAGEPOINT_NAME_2);
            updateTechInfoUsagePoint(usagePoint, upAttributesSet0);
            context.commit();
        }

        try (TransactionContext context = getTransactionService().getContext()) {
            updateTechInfoUsagePoint(usagePoint, upAttributesSet1);
            context.commit();
        }
        testUpdateTechInfoUsagePoint(usagePoint, upAttributesSet0, upAttributesSet1);

        try (TransactionContext context = getTransactionService().getContext()) {
            updateTechInfoUsagePoint(usagePoint, upAttributesSet2);
            context.commit();
        }
        testUpdateTechInfoUsagePoint(usagePoint, upAttributesSet1, upAttributesSet2);
    }

    private void updateTechInfoUsagePoint(UsagePoint usagePoint, Map<TechInfoForElectricityAttribute, Object> to) {
        ElectricityDetailBuilder electricityDetailBuilder = usagePoint.newElectricityDetailBuilder(inMemoryBootstrapModule.getClock().instant());
        to.forEach((key, value) -> key.setValueToObject(electricityDetailBuilder, value));
        electricityDetailBuilder.create();
    }

    private void testUpdateTechInfoUsagePoint(UsagePoint usagePoint, Map<TechInfoForElectricityAttribute, Object> from, Map<TechInfoForElectricityAttribute, Object> to) {
        AuditService auditService = inMemoryBootstrapModule.getAuditService();
        Optional<AuditTrail> auditTrail =  getLastAuditTrail(auditService);
        assertThat(auditTrail).isPresent();
        assertAuditTrail(auditTrail.get(), usagePoint, getDomainContext(), getContextReference(), AuditOperationType.UPDATE);
        assertAuditTrailLog(auditService, from, to);
    }

    private void assertAuditTrailLog(AuditService auditService, Map<TechInfoForElectricityAttribute, Object> from, Map<TechInfoForElectricityAttribute, Object> to) {
        Optional<AuditTrail> auditTrail =  getLastAuditTrail(auditService);
        List<AuditLogChange> auditLogChanges = auditTrail.get().getLogs();
        int auditLogChangesSize = auditLogChanges.size();
        assertThat(auditLogChangesSize).isGreaterThanOrEqualTo(to.size());
        to.forEach((key, value) -> {
            Optional<AuditLogChange> auditLogChange = auditLogChanges.stream()
                    .filter(log -> log.getName().compareToIgnoreCase(key.getName()) == 0)
                    .findFirst();

            assertThat(auditLogChange).isPresent();
            assertThat(auditLogChange.get().getValue()).isEqualTo(value.toString());
            Object fromValue = from.get(key);
            if (fromValue != null) {
                assertThat(auditLogChange.get().getPreviousValue()).isEqualTo(fromValue.toString());
            }
        });
    }

    protected AuditDomainContextType getDomainContext(){
        return AuditDomainContextType.USAGEPOINT_TECHNICAL_ATTRIBUTES;
    }

    protected ImmutableMap getContextReference(){
        return ImmutableMap.of("name", "Technical information");
    }

    protected ServiceKind getServiceCategory(){
        return SERVICE_KIND;
    }


}
