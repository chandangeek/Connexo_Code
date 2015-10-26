package com.elster.jupiter.time.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.RelativePeriodCategory;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.fest.reflect.core.Reflection.field;

@RunWith(MockitoJUnitRunner.class)
public class RelativePeriodCategoryTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;

    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;

    private RelativePeriodCategory relativePeriodCategory;

    private void setId(Object entity, long id) {
        field("id").ofType(Long.TYPE).in(entity).set(id);
    }

    @Override
    protected Object getInstanceA() {
        if (relativePeriodCategory == null) {
            relativePeriodCategory = new RelativePeriodCategoryImpl(dataModel, eventService);
            setId(relativePeriodCategory, ID);
        }
        return relativePeriodCategory;
    }

    @Override
    protected Object getInstanceEqualToA() {
        RelativePeriodCategory relativePeriodCategory = new RelativePeriodCategoryImpl(dataModel, eventService);
        setId(relativePeriodCategory, ID);
        return relativePeriodCategory;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        RelativePeriodCategory relativePeriodCategory = new RelativePeriodCategoryImpl(dataModel, eventService);
        setId(relativePeriodCategory, OTHER_ID);
        return Collections.singletonList(relativePeriodCategory);
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
