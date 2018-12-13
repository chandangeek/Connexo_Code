/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.orm.DataModel;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class EntityEqualsContractTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;

    @Mock
    DataModel dataModel;

    EntityImpl entity;

    static final class ConcreteEntity extends EntityImpl {
        ConcreteEntity(DataModel dataModel) {
            super(dataModel);
        }
    }

    @Override
    protected Object getInstanceA() {
        if (entity == null) {
            entity = new ConcreteEntity(dataModel);
            entity.setId(ID);
        }
        return entity;
    }

    @Override
    protected Object getInstanceEqualToA() {
        EntityImpl entity = new ConcreteEntity(dataModel);
        entity.setId(ID);
        return entity;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        EntityImpl entity = new EntityImpl(dataModel) {};
        entity.setId(OTHER_ID);
        return Collections.singletonList(entity);
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
