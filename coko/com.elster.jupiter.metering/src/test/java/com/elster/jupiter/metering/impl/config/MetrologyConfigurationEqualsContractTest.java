package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.impl.search.UsagePointRequirementsSearchDomain;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import org.fest.reflect.core.Reflection;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Collections.singletonList;

@RunWith(MockitoJUnitRunner.class)
public class MetrologyConfigurationEqualsContractTest extends EqualsContractTest {

    public static final long INSTANCE_A_ID = 54L;

    @Mock
    DataModel dataModel;
    @Mock
    EventService eventService;
    @Mock
    Thesaurus thesaurus;
    @Mock
    ServerMetrologyConfigurationService metrologyConfigurationService;
    @Mock
    UsagePointRequirementsSearchDomain searchDomain;
    @Mock
    private CustomPropertySetService customPropertySetService;

    private MetrologyConfigurationImpl instanceA;

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = new MetrologyConfigurationImpl(metrologyConfigurationService, eventService, this.customPropertySetService);
            Reflection.field("id").ofType(Long.TYPE).in(instanceA).set(INSTANCE_A_ID);
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        MetrologyConfigurationImpl other = new MetrologyConfigurationImpl(metrologyConfigurationService, eventService, customPropertySetService);
        Reflection.field("id").ofType(Long.TYPE).in(other).set(INSTANCE_A_ID);
        return other;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        MetrologyConfigurationImpl other = new MetrologyConfigurationImpl(metrologyConfigurationService, eventService, customPropertySetService);
        Reflection.field("id").ofType(Long.TYPE).in(other).set(INSTANCE_A_ID + 1);
        return singletonList(other);
    }

    @Override
    protected boolean canBeSubclassed() {
        return true;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        UsagePointMetrologyConfigurationImpl subInst = new UsagePointMetrologyConfigurationImpl(metrologyConfigurationService, eventService, this.customPropertySetService, searchDomain);
        Reflection.field("id").ofType(Long.TYPE).in(subInst).set(INSTANCE_A_ID);
        return subInst;
    }
}