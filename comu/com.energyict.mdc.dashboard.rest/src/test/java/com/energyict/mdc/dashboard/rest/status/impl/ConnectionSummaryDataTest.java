package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 9/4/14.
 */
public class ConnectionSummaryDataTest {

    @Test
    public void testSummaryCalculation() throws Exception {
        ConnectionSummaryData data = new ConnectionSummaryData(createConnectionStatusOverview());
        assertThat(data.getFailed()).isEqualTo(56);
        assertThat(data.getPending()).isEqualTo(472);
        assertThat(data.getSuccess()).isEqualTo(113);
        assertThat(data.getTotal()).isEqualTo(411+19+15+113+42+41);
        assertThat(data.getAtLeastOneTaskFailed()).isEqualTo(99);
        assertThat(data.getAllTasksSuccessful()).isEqualTo(14);
    }

    private TaskStatusOverview createConnectionStatusOverview() {
        TaskStatusOverview overview = mock(TaskStatusOverview.class);
        when(overview.getTotalCount()).thenReturn(100L);
        List<Counter<TaskStatus>> counters = new ArrayList<>();
        counters.add(createCounter(TaskStatus.OnHold, 27L));
        counters.add(createCounter(TaskStatus.Busy, 411L));
        counters.add(createCounter(TaskStatus.Retrying, 19L));
        counters.add(createCounter(TaskStatus.NeverCompleted, 15L));
        counters.add(createCounter(TaskStatus.Waiting, 113L));
        counters.add(createCounter(TaskStatus.Pending, 42L));
        counters.add(createCounter(TaskStatus.Failed, 41L));
        when(overview.iterator()).thenReturn(counters.iterator());
        return overview;
    }

    private <C> Counter<C> createCounter(C status, Long count) {
        Counter<C> counter = mock(Counter.class);
        when(counter.getCount()).thenReturn(count);
        when(counter.getCountTarget()).thenReturn(status);
        return counter;
    }


}
