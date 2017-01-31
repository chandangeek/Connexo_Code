/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.export.EventSelectorConfig;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FieldBasedEndDeviceEventTypeFilterTest {

    private EndDeviceEventType type1 = mockType(EndDeviceType.ELECTRIC_METER, EndDeviceDomain.CLOCK, EndDeviceSubDomain.DAYLIGHTSAVINGSTIME, EndDeviceEventOrAction.CHANGED);
    private EndDeviceEventType type2 = mockType(EndDeviceType.ELECTRIC_METER, EndDeviceDomain.FIRMWARE, EndDeviceSubDomain.VERSION, EndDeviceEventOrAction.UPLOADED);
    private EndDeviceEventType type3 = mockType(EndDeviceType.GAS_METER, EndDeviceDomain.CLOCK, EndDeviceSubDomain.TIME, EndDeviceEventOrAction.DECREASED);
    private EndDeviceEventType type4 = mockType(EndDeviceType.GAS_METER, EndDeviceDomain.FIRMWARE, EndDeviceSubDomain.VERSION, EndDeviceEventOrAction.UPLOADED);

    private List<EndDeviceEventType> types = ImmutableList.of(type1, type2, type3, type4);

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DataModel dataModel;
    @Mock
    private MeteringService meteringService;
    @Mock
    private EventSelectorConfig eventSelectorConfig;

    private EndDeviceEventType mockType(EndDeviceType endDeviceType, EndDeviceDomain domain, EndDeviceSubDomain subDomain, EndDeviceEventOrAction eventOrAction) {
        EndDeviceEventType type = mock(EndDeviceEventType.class);
        when(type.getType()).thenReturn(endDeviceType);
        when(type.getDomain()).thenReturn(domain);
        when(type.getSubDomain()).thenReturn(subDomain);
        when(type.getEventOrAction()).thenReturn(eventOrAction);
        return type;
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testMatchAll() {
        FieldBasedEndDeviceEventTypeFilter filter = new FieldBasedEndDeviceEventTypeFilter(meteringService)
                .init(eventSelectorConfig, null, null, null, null);

        List<EndDeviceEventType> filtered = applyFilter(filter);

        assertThat(filtered).isEqualTo(types);
    }

    @Test
    public void testMatchAllCode() {
        FieldBasedEndDeviceEventTypeFilter filter = new FieldBasedEndDeviceEventTypeFilter(meteringService)
                .init(eventSelectorConfig, null, null, null, null);

        assertThat(filter.getCode()).isEqualTo("*.*.*.*");
    }

    @Test
    public void testFilterOnVariableEndDeviceType() {
        FieldBasedEndDeviceEventTypeFilter filter = new FieldBasedEndDeviceEventTypeFilter(meteringService)
                .init(eventSelectorConfig, null, EndDeviceDomain.FIRMWARE, EndDeviceSubDomain.VERSION, EndDeviceEventOrAction.UPLOADED);

        List<EndDeviceEventType> filtered = applyFilter(filter);

        assertThat(filtered).isEqualTo(ImmutableList.of(type2, type4));
    }

    @Test
    public void testFilterOnVariableEndDeviceTypeCode() {
        FieldBasedEndDeviceEventTypeFilter filter = new FieldBasedEndDeviceEventTypeFilter(meteringService)
                .init(eventSelectorConfig, null, EndDeviceDomain.FIRMWARE, EndDeviceSubDomain.VERSION, EndDeviceEventOrAction.UPLOADED);

        assertThat(filter.getCode()).isEqualTo("*.11.124.60");
    }

    @Test
    public void testFilterOnEndDeviceTypeOnly() {
        FieldBasedEndDeviceEventTypeFilter filter = new FieldBasedEndDeviceEventTypeFilter(meteringService)
                .init(eventSelectorConfig, EndDeviceType.GAS_METER, null, null, null);

        List<EndDeviceEventType> filtered = applyFilter(filter);

        assertThat(filtered).isEqualTo(ImmutableList.of(type3, type4));
    }

    @Test
    public void testFilterOnEndDeviceTypeOnlyCode() {
        FieldBasedEndDeviceEventTypeFilter filter = new FieldBasedEndDeviceEventTypeFilter(meteringService)
                .init(eventSelectorConfig, EndDeviceType.GAS_METER, null, null, null);

        assertThat(filter.getCode()).isEqualTo("4.*.*.*");
    }

    @Test
    public void testFilterOnSpecificType() {
        FieldBasedEndDeviceEventTypeFilter filter = new FieldBasedEndDeviceEventTypeFilter(meteringService)
                .init(eventSelectorConfig, EndDeviceType.GAS_METER, EndDeviceDomain.FIRMWARE, EndDeviceSubDomain.VERSION, EndDeviceEventOrAction.UPLOADED);

        List<EndDeviceEventType> filtered = applyFilter(filter);

        assertThat(filtered).isEqualTo(ImmutableList.of(type4));
    }

    @Test
    public void testFilterOnSpecificTypeCode() {
        FieldBasedEndDeviceEventTypeFilter filter = new FieldBasedEndDeviceEventTypeFilter(meteringService)
                .init(eventSelectorConfig, EndDeviceType.GAS_METER, EndDeviceDomain.FIRMWARE, EndDeviceSubDomain.VERSION, EndDeviceEventOrAction.UPLOADED);

        assertThat(filter.getCode()).isEqualTo("4.11.124.60");
    }

    List<EndDeviceEventType> applyFilter(FieldBasedEndDeviceEventTypeFilter filter) {
        return types.stream()
                    .filter(filter.asEndDeviceEventTypePredicate())
                    .collect(Collectors.toList());
    }
}