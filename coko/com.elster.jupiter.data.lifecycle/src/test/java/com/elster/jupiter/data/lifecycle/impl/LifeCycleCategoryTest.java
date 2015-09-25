package com.elster.jupiter.data.lifecycle.impl;

import com.elster.jupiter.data.lifecycle.LifeCycleCategoryKind;
import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class LifeCycleCategoryTest extends EqualsContractTest {
    @Mock
    DataModel dataModel;
    @Mock
    MeteringService meteringService;

    LifeCycleCategoryImpl lifeCycleCategory;

    @Override
    protected Object getInstanceA() {
        if (lifeCycleCategory == null) {
            lifeCycleCategory = new LifeCycleCategoryImpl(dataModel, meteringService);
            lifeCycleCategory.init(LifeCycleCategoryKind.INTERVAL);
        }
        return lifeCycleCategory;
    }

    @Override
    protected Object getInstanceEqualToA() {
        LifeCycleCategoryImpl lifeCycleCategory = new LifeCycleCategoryImpl(dataModel, meteringService);
        lifeCycleCategory.init(LifeCycleCategoryKind.INTERVAL);
        return lifeCycleCategory;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        LifeCycleCategoryImpl lifeCycleCategory = new LifeCycleCategoryImpl(dataModel, meteringService);
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
