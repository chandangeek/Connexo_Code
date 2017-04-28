/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.impl.search.UsagePointRequirementsSearchDomain;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.search.SearchService;

import org.fest.reflect.core.Reflection;

import java.time.Clock;
import java.util.Optional;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetrologyConfigurationEqualsContractTest extends EqualsContractTest {

    public static final long INSTANCE_A_ID = 54L;

    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ServerMetrologyConfigurationService metrologyConfigurationService;
    @Mock
    private UsagePointRequirementsSearchDomain searchDomain;
    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private SearchService searchService;
    @Mock
    private Clock clock;
    @Mock
    private Publisher publisher;

    private MetrologyConfigurationImpl instanceA;

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = new MetrologyConfigurationImpl(dataModel, metrologyConfigurationService, eventService, clock, publisher);
            Reflection.field("id").ofType(Long.TYPE).in(instanceA).set(INSTANCE_A_ID);
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        MetrologyConfigurationImpl other = new MetrologyConfigurationImpl(dataModel, metrologyConfigurationService, eventService, clock, publisher);
        Reflection.field("id").ofType(Long.TYPE).in(other).set(INSTANCE_A_ID);
        return other;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        MetrologyConfigurationImpl other = new MetrologyConfigurationImpl(this.dataModel, this.metrologyConfigurationService, this.eventService, this.clock, this.publisher);
        Reflection.field("id").ofType(Long.TYPE).in(other).set(INSTANCE_A_ID + 1);
        return singletonList(other);
    }

    @Override
    protected boolean canBeSubclassed() {
        return true;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        when(this.searchDomain.getId()).thenReturn("UsagePoint");
        when(this.searchService.findDomain(any())).thenReturn(Optional.of(this.searchDomain));
        UsagePointMetrologyConfigurationImpl subInst = new UsagePointMetrologyConfigurationImpl(this.dataModel, this.metrologyConfigurationService, this.eventService, this.customPropertySetService, this.searchDomain, this.searchService, this.clock, publisher);
        Reflection.field("id").ofType(Long.TYPE).in(subInst).set(INSTANCE_A_ID);
        return subInst;
    }
}