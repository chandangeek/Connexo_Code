package com.elster.insight.usagepoint.config.impl;

import static java.util.Collections.singletonList;
import static org.fest.reflect.core.Reflection.field;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.validation.ValidationService;

@RunWith(MockitoJUnitRunner.class)
public class MetrologyConfigurationEqualsContractTest extends EqualsContractTest {

	public static final long INSTANCE_A_ID = 54L;
	
    @Mock
    DataModel dataModel;
    @Mock
    EventService eventService;
    @Mock
    ValidationService validationService;

    private MetrologyConfigurationImpl instanceA;

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = new MetrologyConfigurationImpl(dataModel, eventService, validationService);
            instanceA.init("name");
            field("id").ofType(Long.TYPE).in(instanceA).set(INSTANCE_A_ID);

        }
        return instanceA;
    }
    
    @Override
    protected Object getInstanceEqualToA() {
    	MetrologyConfigurationImpl other = new MetrologyConfigurationImpl(dataModel, eventService, validationService);
        other.init("name");
        field("id").ofType(Long.TYPE).in(other).set(INSTANCE_A_ID);
        return other;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
    	MetrologyConfigurationImpl other = new MetrologyConfigurationImpl(dataModel, eventService, validationService);
        other.init("name");
        field("id").ofType(Long.TYPE).in(other).set(INSTANCE_A_ID + 1);
        return singletonList(other);
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
