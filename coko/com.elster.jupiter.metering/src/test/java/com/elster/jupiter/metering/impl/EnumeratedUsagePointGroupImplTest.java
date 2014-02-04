package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointMembership;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.IntermittentInterval;
import com.elster.jupiter.util.time.Interval;
import org.joda.time.DateMidnight;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnumeratedUsagePointGroupImplTest {

    @Mock
    private DataModel dataModel;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private DataMapper<EnumeratedUsagePointGroup.Entry> entryMapper;

    @Before
    public void setUp() {
        when(dataModel.mapper(EnumeratedUsagePointGroup.Entry.class)).thenReturn(entryMapper);
        when(dataModel.getInstance(EnumeratedUsagePointGroupImpl.EntryImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new EnumeratedUsagePointGroupImpl.EntryImpl(dataModel);
            }
        });
    }

    @After
    public void tearDown() {

    }
    @Test
    public void test() {
        EnumeratedUsagePointGroupImpl group = new EnumeratedUsagePointGroupImpl(dataModel);
        group.add(usagePoint, interval(date(2013, 2, 3), date(2013, 2, 14)));
        group.add(usagePoint, interval(date(2013, 2, 20), date(2013, 2, 27)));

        List<UsagePointMembership> members = group.getMembers(interval(date(2013, 2, 12), date(2013, 3, 5)));

        assertThat(members).hasSize(1);
        UsagePointMembership usagePointMembership = members.get(0);
        assertThat(usagePointMembership.getIntervals()).isEqualTo(new IntermittentInterval(interval(date(2013, 2, 12), date(2013, 2, 14)),
                interval(date(2013, 2, 20), date(2013, 2, 27))));
        assertThat(usagePointMembership.getUsagePoint()).isEqualTo(usagePoint);

    }

    private Interval interval(Date start, Date end) {
        return new Interval(start, end);
    }

    private Date date(int year, int monthOfYear, int dayOfMonth) {
        return new DateMidnight(year, monthOfYear, dayOfMonth).toDate();
    }


}
