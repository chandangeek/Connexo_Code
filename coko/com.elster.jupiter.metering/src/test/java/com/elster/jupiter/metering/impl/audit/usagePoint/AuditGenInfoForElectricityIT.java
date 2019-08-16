/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.usagePoint;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.impl.audit.usagePoint.attributes.GeneralInfoAttribute;
import com.elster.jupiter.transaction.TransactionContext;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import org.junit.Test;

public class AuditGenInfoForElectricityIT extends AuditAttributeBase {

    private static final String USAGEPOINT_NAME = "UPName";
    private static final String USAGEPOINT_NAME_2 = "UPName2";
    private static final ServiceKind SERVICE_KIND = ServiceKind.ELECTRICITY;

    private static final Map<GeneralInfoAttribute, Object> upAttributesSet0 =  ImmutableMap.of(
            GeneralInfoAttribute.NAME, USAGEPOINT_NAME,
            GeneralInfoAttribute.LIFE_CYCLE, "Default life cycle",
            GeneralInfoAttribute.SERVICE, SERVICE_KIND.getDefaultFormat()
    );
    private static final Map<GeneralInfoAttribute, Object> upAttributesSet1 =  ImmutableMap.of(
            GeneralInfoAttribute.ROUTE, "ROUTE1"
    );
    private static final Map<GeneralInfoAttribute, Object> upAttributesSet2 =  ImmutableMap.of(
            GeneralInfoAttribute.ROUTE, "ROUTE2"
    );

    @Test
    @Transactional
    public void createUsagePointTest() {
        testCreateUsagePoint(upAttributesSet0);
    }

    @Test
    public void updateUsagePointTest() {
        UsagePoint usagePoint;
        try (TransactionContext context = getTransactionService().getContext()) {
            usagePoint = createUsagePoint(USAGEPOINT_NAME_2);
            context.commit();
        }

        try (TransactionContext context = getTransactionService().getContext()) {
            updateUsagePoint(usagePoint, upAttributesSet1);
            context.commit();
        }
        testUpdateUsagePoint(usagePoint, upAttributesSet0, upAttributesSet1);

        try (TransactionContext context = getTransactionService().getContext()) {
            updateUsagePoint(usagePoint, upAttributesSet2);
            context.commit();
        }
        testUpdateUsagePoint(usagePoint, upAttributesSet1, upAttributesSet2);
    }

    protected AuditDomainContextType getDomainContext(){
        return AuditDomainContextType.USAGEPOINT_GENERAL_ATTRIBUTES;
    }

    protected ImmutableMap getContextReference(){
        return ImmutableMap.of("name", "General information");
    }

    protected ServiceKind getServiceKind(){
        return SERVICE_KIND;
    }


}
