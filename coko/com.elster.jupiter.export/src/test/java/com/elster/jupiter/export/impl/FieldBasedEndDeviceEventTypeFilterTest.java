package com.elster.jupiter.export.impl;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FieldBasedEndDeviceEventTypeFilterTest {

    private EndDeviceEventType type1 = mockType(EndDeviceType.ELECTRIC_METER, EndDeviceDomain.CLOCK, EndDeviceSubDomain.DAYLIGHTSAVINGSTIME, EndDeviceEventorAction.CHANGED);
    private EndDeviceEventType type2 = mockType(EndDeviceType.ELECTRIC_METER, EndDeviceDomain.FIRMWARE, EndDeviceSubDomain.VERSION, EndDeviceEventorAction.UPLOADED);
    private EndDeviceEventType type3 = mockType(EndDeviceType.GAS_METER, EndDeviceDomain.CLOCK, EndDeviceSubDomain.TIME, EndDeviceEventorAction.DECREASED);
    private EndDeviceEventType type4 = mockType(EndDeviceType.GAS_METER, EndDeviceDomain.FIRMWARE, EndDeviceSubDomain.VERSION, EndDeviceEventorAction.UPLOADED);

    private List<EndDeviceEventType> types = ImmutableList.of(type1, type2, type3, type4);

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DataModel dataModel;
    @Mock
    private IReadingTypeDataSelector selector;

    private EndDeviceEventType mockType(EndDeviceType endDeviceType, EndDeviceDomain domain, EndDeviceSubDomain subDomain, EndDeviceEventorAction eventOrAction) {
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
        FieldBasedEndDeviceEventTypeFilter filter = new FieldBasedEndDeviceEventTypeFilter()
                .init(selector, null, null, null, null);

        List<EndDeviceEventType> filtered = applyFilter(filter);

        assertThat(filtered).isEqualTo(types);
    }

    @Test
    public void testMatchAllCode() {
        FieldBasedEndDeviceEventTypeFilter filter = new FieldBasedEndDeviceEventTypeFilter()
                .init(selector, null, null, null, null);

        assertThat(filter.getCode()).isEqualTo("*.*.*.*");
    }

    @Test
    public void testFilterOnVariableEndDeviceType() {
        FieldBasedEndDeviceEventTypeFilter filter = new FieldBasedEndDeviceEventTypeFilter()
                .init(selector, null, EndDeviceDomain.FIRMWARE, EndDeviceSubDomain.VERSION, EndDeviceEventorAction.UPLOADED);

        List<EndDeviceEventType> filtered = applyFilter(filter);

        assertThat(filtered).isEqualTo(ImmutableList.of(type2, type4));
    }

    @Test
    public void testFilterOnVariableEndDeviceTypeCode() {
        FieldBasedEndDeviceEventTypeFilter filter = new FieldBasedEndDeviceEventTypeFilter()
                .init(selector, null, EndDeviceDomain.FIRMWARE, EndDeviceSubDomain.VERSION, EndDeviceEventorAction.UPLOADED);

        assertThat(filter.getCode()).isEqualTo("*.11.124.60");
    }

    @Test
    public void testFilterOnEndDeviceTypeOnly() {
        FieldBasedEndDeviceEventTypeFilter filter = new FieldBasedEndDeviceEventTypeFilter()
                .init(selector, EndDeviceType.GAS_METER, null, null, null);

        List<EndDeviceEventType> filtered = applyFilter(filter);

        assertThat(filtered).isEqualTo(ImmutableList.of(type3, type4));
    }

    @Test
    public void testFilterOnEndDeviceTypeOnlyCode() {
        FieldBasedEndDeviceEventTypeFilter filter = new FieldBasedEndDeviceEventTypeFilter()
                .init(selector, EndDeviceType.GAS_METER, null, null, null);

        assertThat(filter.getCode()).isEqualTo("4.*.*.*");
    }

    @Test
    public void testFilterOnSpecificType() {
        FieldBasedEndDeviceEventTypeFilter filter = new FieldBasedEndDeviceEventTypeFilter()
                .init(selector, EndDeviceType.GAS_METER, EndDeviceDomain.FIRMWARE, EndDeviceSubDomain.VERSION, EndDeviceEventorAction.UPLOADED);

        List<EndDeviceEventType> filtered = applyFilter(filter);

        assertThat(filtered).isEqualTo(ImmutableList.of(type4));
    }

    @Test
    public void testFilterOnSpecificTypeCode() {
        FieldBasedEndDeviceEventTypeFilter filter = new FieldBasedEndDeviceEventTypeFilter()
                .init(selector, EndDeviceType.GAS_METER, EndDeviceDomain.FIRMWARE, EndDeviceSubDomain.VERSION, EndDeviceEventorAction.UPLOADED);

        assertThat(filter.getCode()).isEqualTo("4.11.124.60");
    }

    List<EndDeviceEventType> applyFilter(FieldBasedEndDeviceEventTypeFilter filter) {
        return types.stream()
                    .filter(filter.asEndDeviceEventTypePredicate())
                    .collect(Collectors.toList());
    }


}