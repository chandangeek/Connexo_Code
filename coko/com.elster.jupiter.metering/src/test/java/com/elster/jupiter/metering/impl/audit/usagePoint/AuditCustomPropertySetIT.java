/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.usagePoint;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.AuditOperationType;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.AuditTrail;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointVersionedPropertySet;
import com.elster.jupiter.metering.impl.cps.CustomPropertySetAttributes;
import com.elster.jupiter.transaction.TransactionContext;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditCustomPropertySetIT extends AuditAttributeBaseTest {

    private static final String USAGEPOINT_NAME_2 = "UPName2";
    private static final ServiceKind SERVICE_KIND = ServiceKind.ELECTRICITY;

    private static final Map<CustomPropertySetAttributes, Object> upAttributesSet0 =  ImmutableMap.of(
            CustomPropertySetAttributes.NAME, "Nane0",
            CustomPropertySetAttributes.ENHANCED_SUPPORT, Boolean.TRUE
    );
    private static final Map<CustomPropertySetAttributes, Object> upAttributesSet1 =  ImmutableMap.of(
            CustomPropertySetAttributes.NAME, "Nane1",
            CustomPropertySetAttributes.ENHANCED_SUPPORT, Boolean.FALSE
    );
    private static final Map<CustomPropertySetAttributes, Object> upAttributesSet2 =  ImmutableMap.of(
            CustomPropertySetAttributes.NAME, "Nane2",
            CustomPropertySetAttributes.ENHANCED_SUPPORT, Boolean.TRUE
    );

    @Test
    public void updateUsagePointTest() {
        UsagePoint usagePoint;
        try (TransactionContext context = getTransactionService().getContext()) {
            usagePoint = createUsagePoint(USAGEPOINT_NAME_2);
            addCustomPropertySetToServiceCategory();
            grantViewPrivilegesForCurrentUser();
            grantEditPrivilegesForCurrentUser();
            updatePropertyValues(usagePoint, upAttributesSet0);
            context.commit();
        }

        try (TransactionContext context = getTransactionService().getContext()) {
            updatePropertyValues(usagePoint, upAttributesSet1);
            context.commit();
        }
        testCustomPropertySet(usagePoint, upAttributesSet0, upAttributesSet1);

        try (TransactionContext context = getTransactionService().getContext()) {
            updatePropertyValues(usagePoint, upAttributesSet2);
            context.commit();
        }
        testCustomPropertySet(usagePoint, upAttributesSet1, upAttributesSet2);
    }

    private void updatePropertyValues(UsagePoint usagePoint, Map<CustomPropertySetAttributes, Object> attributesSet) {
        CustomPropertySetValues values = CustomPropertySetValues.emptyFrom(usagePoint.getInstallationTime());
        attributesSet.entrySet().stream()
                .forEach(customPropertySetAttributesObjectEntry -> values.setProperty(customPropertySetAttributesObjectEntry.getKey().propertyKey(),
                        customPropertySetAttributesObjectEntry.getValue()));
        UsagePointVersionedPropertySet versionedPropertySet = (UsagePointVersionedPropertySet) usagePoint.forCustomProperties().getAllPropertySets().get(0);
        versionedPropertySet.setValues(values);
    }

    private void testCustomPropertySet(UsagePoint usagePoint, Map<CustomPropertySetAttributes, Object> from, Map<CustomPropertySetAttributes, Object> to) {
        AuditService auditService = inMemoryBootstrapModule.getAuditService();
        Optional<AuditTrail> auditTrail =  getLastAuditTrail(auditService);
        assertThat(auditTrail).isPresent();
        assertAuditTrail(auditTrail.get(), usagePoint, getDomainContext(), getContextReference(), AuditOperationType.UPDATE, 1);
        assertAuditTrailLog(auditService, from, to);
    }

    private void assertAuditTrailLog(AuditService auditService, Map<CustomPropertySetAttributes, Object> from, Map<CustomPropertySetAttributes, Object> to) {
        Optional<AuditTrail> auditTrail =  getLastAuditTrail(auditService);
        List<AuditLogChange> auditLogChanges = auditTrail.get().getLogs();
        int auditLogChangesSize = auditLogChanges.size();
        assertThat(auditLogChangesSize).isGreaterThanOrEqualTo(to.size());
        to.forEach((key, value) -> {
            Optional<AuditLogChange> auditLogChange = auditLogChanges.stream()
                    .filter(log -> log.getName().compareToIgnoreCase(key.propertyKey()) == 0)
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
        return AuditDomainContextType.USAGEPOINT_CUSTOM_ATTRIBUTES;
    }

    protected ImmutableMap getContextReference(){
        return ImmutableMap.of("name", "UsagePointTestCustomPropertySet",
                "startTime", Instant.EPOCH,
                "isVersioned", true);
    }

    protected ServiceKind getServiceKind(){
        return SERVICE_KIND;
    }


}
