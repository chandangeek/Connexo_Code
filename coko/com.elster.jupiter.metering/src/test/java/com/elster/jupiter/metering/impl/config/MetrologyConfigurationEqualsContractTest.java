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
            instanceA = new MetrologyConfigurationImpl(dataModel, metrologyConfigurationService, eventService, this.customPropertySetService, clock, publisher);
            Reflection.field("id").ofType(Long.TYPE).in(instanceA).set(INSTANCE_A_ID);
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        MetrologyConfigurationImpl other = new MetrologyConfigurationImpl(dataModel, metrologyConfigurationService, eventService, customPropertySetService, clock, publisher);
        Reflection.field("id").ofType(Long.TYPE).in(other).set(INSTANCE_A_ID);
        return other;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        MetrologyConfigurationImpl other = new MetrologyConfigurationImpl(dataModel, metrologyConfigurationService, eventService, customPropertySetService, clock, publisher);
        Reflection.field("id").ofType(Long.TYPE).in(other).set(INSTANCE_A_ID + 1);
        return singletonList(other);
    }

    @Override
    protected boolean canBeSubclassed() {
        return true;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        when(searchDomain.getId()).thenReturn("UsagePoint");
        when(searchService.findDomain(any())).thenReturn(Optional.of(searchDomain));
        UsagePointMetrologyConfigurationImpl subInst = new UsagePointMetrologyConfigurationImpl(dataModel, metrologyConfigurationService, eventService, this.customPropertySetService, searchDomain, searchService, clock, publisher);
        Reflection.field("id").ofType(Long.TYPE).in(subInst).set(INSTANCE_A_ID);
        return subInst;
    }
}