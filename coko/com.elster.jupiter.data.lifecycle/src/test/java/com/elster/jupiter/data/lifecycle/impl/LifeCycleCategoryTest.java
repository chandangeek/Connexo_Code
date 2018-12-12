/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.data.lifecycle.impl;

import com.elster.jupiter.data.lifecycle.LifeCycleCategoryKind;
import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import java.util.Collections;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LifeCycleCategoryTest extends EqualsContractTest {
    @Mock
    private DataModel dataModel;
    @Mock
    private MeteringService meteringService;
    @Mock
    private Thesaurus thesaurus;

    private LifeCycleCategoryImpl lifeCycleCategory;

    @Override
    protected Object getInstanceA() {
        if (lifeCycleCategory == null) {
            lifeCycleCategory = new LifeCycleCategoryImpl(dataModel, thesaurus, meteringService);
            lifeCycleCategory.init(LifeCycleCategoryKind.INTERVAL);
        }
        return lifeCycleCategory;
    }

    @Override
    protected Object getInstanceEqualToA() {
        LifeCycleCategoryImpl lifeCycleCategory = new LifeCycleCategoryImpl(dataModel, thesaurus, meteringService);
        lifeCycleCategory.init(LifeCycleCategoryKind.INTERVAL);
        return lifeCycleCategory;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        LifeCycleCategoryImpl lifeCycleCategory = new LifeCycleCategoryImpl(dataModel, thesaurus, meteringService);
        lifeCycleCategory.init(LifeCycleCategoryKind.JOURNAL);
        return Collections.singletonList(lifeCycleCategory);
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}
