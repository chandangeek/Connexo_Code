/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.GroupEventData;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.impl.search.DeviceGroupSearchableProperty;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceGroupDeletionVetoEventHandlerTest {

    private DeviceGroupDeletionVetoEventHandler deviceGroupDeletionVetoEventHandler;
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
    private EndDeviceGroup endDeviceGroup;
    @Mock
    private QueryEndDeviceGroup queryEndDeviceGroup;
    @Mock
    private Query<EndDeviceGroup> endDeviceGroupQuery;
    @Mock
    private DeviceGroupSearchableProperty deviceGroupSearchableProperty;
    @Mock
    private PropertySpec propertySpec;
    @Mock
    private ValueFactory valueFactory;

    @Before
    public void setUp() {
        deviceGroupDeletionVetoEventHandler = new DeviceGroupDeletionVetoEventHandler(meteringGroupsService, thesaurus);
        valueBean = new SearchablePropertyValue.ValueBean(DeviceGroupSearchableProperty.PROPERTY_NAME, SearchablePropertyOperator.EQUAL);
        searchablePropertyValue = new SearchablePropertyValue(deviceGroupSearchableProperty, valueBean);

        when(localEvent.getSource()).thenReturn(eventSource);
        when(meteringGroupsService.getQueryEndDeviceGroupQuery()).thenReturn(endDeviceGroupQuery);
        when(endDeviceGroupQuery.select(Condition.TRUE)).thenReturn(Collections.singletonList(queryEndDeviceGroup));
        doReturn(endDeviceGroup).when(eventSource).getGroup();
        when(queryEndDeviceGroup.getSearchablePropertyValues()).thenReturn(Collections.singletonList(searchablePropertyValue));
        when(queryEndDeviceGroup.isDynamic()).thenReturn(true);
        when(deviceGroupSearchableProperty.getSpecification()).thenReturn(propertySpec);
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        when(valueFactory.fromStringValue(any())).thenReturn(endDeviceGroup);
        when(deviceGroupSearchableProperty.getName()).thenReturn(DeviceGroupSearchableProperty.PROPERTY_NAME);
    }

    @Test(expected = VetoDeleteDeviceGroupException.class)
    public void catchVetoDeleteDeviceGroupException() {
        valueBean.setValues(Collections.singletonList(String.valueOf(queryEndDeviceGroup.getId())));

        deviceGroupDeletionVetoEventHandler.handle(localEvent);
    }

    @Test
    public void noVetoDeleteDeviceGroupException() {
        valueBean.setValues(Collections.emptyList());
        deviceGroupDeletionVetoEventHandler.handle(localEvent);

        // Asserts
        // no exception
    }
}
