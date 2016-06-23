package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.UsagePointMembership;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnumeratedUsagePointGroupImplTest {

    @Mock
    private DataModel dataModel;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private DataMapper<EnumeratedUsagePointGroupImpl.EntryImpl> entryMapper;
    @Mock
    private MeteringService meteringService;

    @Before
    public void setUp() {
        when(dataModel.mapper(EnumeratedUsagePointGroupImpl.EntryImpl.class)).thenReturn(entryMapper);
        when(dataModel.getInstance(EnumeratedUsagePointGroupImpl.EntryImpl.class)).thenAnswer(invocationOnMock -> new EnumeratedUsagePointGroupImpl.EntryImpl(dataModel, meteringService));
    }

    @Test
    public void testGetMembersRange() {
        EnumeratedUsagePointGroupImpl group = new EnumeratedUsagePointGroupImpl(dataModel);
        group.add(usagePoint, range(instant(2013, 2, 3), instant(2013, 2, 14)));
        group.add(usagePoint, range(instant(2013, 2, 20), instant(2013, 2, 27)));

        List<UsagePointMembership> members = group.getMembers(range(instant(2013, 2, 12), instant(2013, 3, 5)));

        assertThat(members).hasSize(1);
        UsagePointMembership usagePointMembership = members.get(0);
        assertThat(usagePointMembership.getRanges()).isEqualTo(ImmutableRangeSet.<Instant>builder()
        		.add(range(instant(2013, 2, 12), instant(2013, 2, 14)))
                .add(range(instant(2013, 2, 20), instant(2013, 2, 27)))
                .build());
        assertThat(usagePointMembership.getUsagePoint()).isEqualTo(usagePoint);

    }

    private Range<Instant> range(Instant start, Instant end) {
        return Range.closedOpen(start,end);
    }

    private Instant instant(int year, int monthOfYear, int dayOfMonth) {
        return LocalDate.of(year, monthOfYear, dayOfMonth).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }


}
