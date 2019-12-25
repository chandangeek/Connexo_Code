/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.usagePoint;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.AuditTrail;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.transaction.TransactionContext;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditMetrologyConfigurationIT extends AuditAttributeBaseTest {

    private static final String USAGEPOINT_NAME = "UPName";
    private static final String METROLOGY_CONFIGURATION_MRID = "metrologyConfiguration";
    private static final String METROLOGY_CONFIGURATION_MRID2 = "metrologyConfiguration2";
    private static final ServiceKind SERVICE_KIND = ServiceKind.ELECTRICITY;

    private static final String METROLOGY_START_DATE = "Metrology start date";
    private static final String METROLOGY_END_DATE = "Metrology end date";

    @Test
    public void testMetrologyConfiguration() {
        UsagePoint usagePoint;
        UsagePointMetrologyConfiguration metrologyConfiguration;
        UsagePointMetrologyConfiguration metrologyConfiguration2;
        Instant start = Instant.now();
        Instant end  = start.plusSeconds(60);

        try (TransactionContext context = getTransactionService().getContext()) {
            metrologyConfiguration = createMetrologyConfiguration(METROLOGY_CONFIGURATION_MRID);
            metrologyConfiguration2 = createMetrologyConfiguration(METROLOGY_CONFIGURATION_MRID2);
            context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
            usagePoint = createUsagePoint(USAGEPOINT_NAME);
            linkUsagePointToMetrologyConfiguration(usagePoint, metrologyConfiguration, start, end);
            addCustomPropertySetToMetrologyConfiguration(metrologyConfiguration);
            context.commit();
        }
        assertAuditTrailLog(METROLOGY_CONFIGURATION_MRID, METROLOGY_END_DATE);
        try (TransactionContext context = getTransactionService().getContext()) {
            linkUsagePointToMetrologyConfiguration(usagePoint, metrologyConfiguration2, end);
            addCustomPropertySetToMetrologyConfiguration(metrologyConfiguration2);
            context.commit();
        }
        assertAuditTrailLog(METROLOGY_CONFIGURATION_MRID2, METROLOGY_START_DATE);
    }

    private void assertAuditTrailLog(String metrologyName, String propertyName) {
        AuditService auditService = inMemoryBootstrapModule.getAuditService();
        Optional<AuditTrail> auditTrail =  getLastAuditTrail(auditService);
        List<AuditLogChange> auditLogChanges = auditTrail.get().getLogs();
        int auditLogChangesSize = auditLogChanges.size();
        assertThat(auditLogChangesSize).isGreaterThanOrEqualTo(2);

        Optional<AuditLogChange> metrologyNameLog = auditLogChanges.stream()
                .filter(log -> log.getName().compareToIgnoreCase("Metrology configuration") == 0)
                .findFirst();
        assertThat(metrologyNameLog).isPresent();
        assertThat(metrologyNameLog.get().getValue()).isEqualTo(metrologyName);

        Optional<AuditLogChange> metrologyStartDateLog = auditLogChanges.stream()
                .filter(log -> log.getName().compareToIgnoreCase(propertyName) == 0)
                .findFirst();
        assertThat(metrologyStartDateLog).isPresent();
    }

    protected AuditDomainContextType getDomainContext(){
        return AuditDomainContextType.USAGEPOINT_METROLOGY_CONFIGURATION;
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
