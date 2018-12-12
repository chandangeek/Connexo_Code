/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;

import java.util.Arrays;

public class KpiTypeEqualsContractTest extends EqualsContractTest {

    private static final String WITH_CLAUSE_ALIAS = "withClauseAlias";
    private static final String KPI_TABLE_NAME = "kpiTableName";
    private static final String KPI_TYPE = "kpiType";

    private KpiType instanceA = new KpiType(WITH_CLAUSE_ALIAS, KPI_TABLE_NAME, KPI_TYPE);

    @Override
    protected Object getInstanceA() {
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new KpiType(WITH_CLAUSE_ALIAS, KPI_TABLE_NAME, KPI_TYPE);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(KpiType.ADDED, KpiType.EDITED, KpiType.REMOVED);
    }

    @Override
    protected boolean canBeSubclassed() {
        return true;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return new KpiType(WITH_CLAUSE_ALIAS, KPI_TABLE_NAME, KPI_TYPE) {
        };
    }
}
