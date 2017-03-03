/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.groups.GroupEventData;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.metering.groups.impl.search.UsagePointGroupSearchableProperty;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.conditions.Condition;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointGroupDeletionVetoEventHandlerTest {

    private UsagePointGroupDeletionVetoEventHandler usagePointGroupDeletionVetoEventHandler;
    private SearchablePropertyValue searchablePropertyValue;
    private SearchablePropertyValue.ValueBean valueBean;

    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private LocalEvent localEvent;
    @Mock
    private GroupEventData eventSource;
    @Mock
    private UsagePointGroup usagePointGroup;
    @Mock
    private QueryUsagePointGroup queryUsagePointGroup;
    @Mock
    private Query<UsagePointGroup> usagePointGroupQuery;
    @Mock
    private UsagePointGroupSearchableProperty usagePointGroupSearchableProperty;
    @Mock
    private PropertySpec propertySpec;
    @Mock
    private ValueFactory valueFactory;

    @Before
    public void setUp() {
        usagePointGroupDeletionVetoEventHandler = new UsagePointGroupDeletionVetoEventHandler(meteringGroupsService, thesaurus);
        valueBean = new SearchablePropertyValue.ValueBean(UsagePointGroupSearchableProperty.PROPERTY_NAME, SearchablePropertyOperator.EQUAL);
        searchablePropertyValue = new SearchablePropertyValue(usagePointGroupSearchableProperty, valueBean);

        when(localEvent.getSource()).thenReturn(eventSource);
        when(meteringGroupsService.getQueryUsagePointGroupQuery()).thenReturn(usagePointGroupQuery);
        when(usagePointGroupQuery.select(Condition.TRUE)).thenReturn(Collections.singletonList(queryUsagePointGroup));
        doReturn(usagePointGroup).when(eventSource).getGroup();
        when(queryUsagePointGroup.getSearchablePropertyValues()).thenReturn(Collections.singletonList(searchablePropertyValue));
        when(queryUsagePointGroup.isDynamic()).thenReturn(true);
        when(usagePointGroupSearchableProperty.getSpecification()).thenReturn(propertySpec);
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        when(valueFactory.fromStringValue(any())).thenReturn(usagePointGroup);
        when(usagePointGroupSearchableProperty.getName()).thenReturn(UsagePointGroupSearchableProperty.PROPERTY_NAME);
    }

    @Test(expected = VetoDeleteUsagePointGroupException.class)
    public void catchVetoDeleteDeviceGroupException() {
        valueBean.setValues(String.valueOf(queryUsagePointGroup.getId()));
        usagePointGroupDeletionVetoEventHandler.handle(localEvent);
    }

    @Test
    public void noVetoDeleteDeviceGroupException() {
        valueBean.setValues(Collections.emptyList());
        usagePointGroupDeletionVetoEventHandler.handle(localEvent);

        // Asserts
        // no exception
    }
}
