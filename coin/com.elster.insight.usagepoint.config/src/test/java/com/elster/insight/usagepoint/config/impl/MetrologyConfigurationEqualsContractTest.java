package com.elster.insight.usagepoint.config.impl;

import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.validation.ValidationService;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Collections.singletonList;
import static org.fest.reflect.core.Reflection.field;

@RunWith(MockitoJUnitRunner.class)
public class MetrologyConfigurationEqualsContractTest extends EqualsContractTest {

    public static final long INSTANCE_A_ID = 54L;

    @Mock
    DataModel dataModel;
    @Mock
    EventService eventService;
    @Mock
    ValidationService validationService;
    @Mock
    Thesaurus thesaurus;
    @Mock
    UsagePointConfigurationService usagePointConfigurationService;

    private MetrologyConfigurationImpl instanceA;

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = new MetrologyConfigurationImpl(dataModel, eventService, validationService, thesaurus, usagePointConfigurationService);
            instanceA.init("name");
            field("id").ofType(Long.TYPE).in(instanceA).set(INSTANCE_A_ID);

        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        MetrologyConfigurationImpl other = new MetrologyConfigurationImpl(dataModel, eventService, validationService, thesaurus, usagePointConfigurationService);
        other.init("name");
        field("id").ofType(Long.TYPE).in(other).set(INSTANCE_A_ID);
        return other;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        MetrologyConfigurationImpl other = new MetrologyConfigurationImpl(dataModel, eventService, validationService, thesaurus, usagePointConfigurationService);
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
