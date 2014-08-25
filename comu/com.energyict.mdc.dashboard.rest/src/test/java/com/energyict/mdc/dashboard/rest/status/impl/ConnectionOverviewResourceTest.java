package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.dashboard.ComPortPoolBreakdown;
import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.ConnectionTaskHeatMapRow;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.ConnectionTaskDeviceTypeHeatMap;
import com.energyict.mdc.dashboard.TaskStatusBreakdownCounter;
import com.energyict.mdc.dashboard.impl.ComSessionSuccessIndicatorOverviewImpl;
import com.energyict.mdc.dashboard.impl.ConnectionTaskHeatMapRowImpl;
import com.energyict.mdc.dashboard.impl.TaskStatusBreakdownCounterImpl;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.status.StatusService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.dashboard.rest.status.ComServerStatusResource} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (17:27)
 */
public class ConnectionOverviewResourceTest extends JerseyTest {

    @Mock
    private StatusService statusService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DashboardService dashboardService;

    @Before
    public void setupMocks () {
        when(thesaurus.getString(anyString(), anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                for (MessageSeeds messageSeeds : MessageSeeds.values()) {
                    if (messageSeeds.getKey().equals(invocationOnMock.getArguments()[0])) {
                        return messageSeeds.getDefaultFormat();
                    }
                }
                return (String) invocationOnMock.getArguments()[1];
            }
        });
        NlsMessageFormat mft = mock(NlsMessageFormat.class);
        when(mft.format(any(Object[].class))).thenReturn("format");
        when(thesaurus.getFormat(Matchers.<MessageSeed>anyObject())).thenReturn(mft);

    }

    private ComSessionSuccessIndicatorOverview createComTaskCompletionOverview() {
        ComSessionSuccessIndicatorOverview overview = mock(ComSessionSuccessIndicatorOverview.class);
        when(overview.getTotalCount()).thenReturn(100L);
        List<Counter<ComSession.SuccessIndicator>> counters = new ArrayList<>();
        counters.add(createCounter(ComSession.SuccessIndicator.Success, 101L));
        counters.add(createCounter(ComSession.SuccessIndicator.Broken, 12L));
        counters.add(createCounter(ComSession.SuccessIndicator.SetupError, 41L));
        when(overview.iterator()).thenReturn(counters.iterator());
        return overview;
    }

    private TaskStatusOverview createConnectionStatusOverview() {
        TaskStatusOverview overview = mock(TaskStatusOverview.class);
        when(overview.getTotalCount()).thenReturn(100L);
        List<Counter<TaskStatus>> counters = new ArrayList<>();
        counters.add(createCounter(TaskStatus.Busy, 41L));
        counters.add(createCounter(TaskStatus.OnHold, 27L));
        counters.add(createCounter(TaskStatus.Retrying, 15L));
        counters.add(createCounter(TaskStatus.NeverCompleted, 15L));
        counters.add(createCounter(TaskStatus.Waiting, 15L));
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

    @Override
    protected Application configure() {
        MockitoAnnotations.initMocks(this);
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(
                ConnectionOverviewResource.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedFieldValidationExceptionMapper.class,
                LocalizedExceptionMapper.class);
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(statusService).to(StatusService.class);
                bind(nlsService).to(NlsService.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(dashboardService).to(DashboardService.class);
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(ExceptionFactory.class).to(ExceptionFactory.class);
            }
        });
        return resourceConfig;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(JacksonFeature.class); // client side JSON processing
        super.configureClient(config);
    }

    @Test
    public void testGetOverview() throws UnsupportedEncodingException {
        TaskStatusOverview taskStatusOverview = createConnectionStatusOverview();
        when(dashboardService.getConnectionTaskStatusOverview()).thenReturn(taskStatusOverview);
        ComSessionSuccessIndicatorOverview comSessionSuccessIndicatorOverview = createComTaskCompletionOverview();
        when(dashboardService.getComSessionSuccessIndicatorOverview()).thenReturn(comSessionSuccessIndicatorOverview);
        ComPortPoolBreakdown comPortPoolBreakdown = createComPortPoolBreakdown();
        when(dashboardService.getComPortPoolBreakdown()).thenReturn(comPortPoolBreakdown);
        ConnectionTypeBreakdown connectionStatusBreakdown = createConnectionTypeBreakdown();
        when(dashboardService.getConnectionTypeBreakdown()).thenReturn(connectionStatusBreakdown);
        DeviceTypeBreakdown deviceTypeBreakdown=createDeviceTypeBreakdown();
        when(dashboardService.getConnectionTasksDeviceTypeBreakdown()).thenReturn(deviceTypeBreakdown);
        ConnectionTaskDeviceTypeHeatMap heatMap = createDeviceTypeHeatMap();
        when(dashboardService.getConnectionsDeviceTypeHeatMap()).thenReturn(heatMap);


        ConnectionOverviewInfo connectionOverviewInfo = target("/connectionoverview").queryParam("filter", ExtjsFilter.filter("breakdown", BreakdownOption.deviceType.name())).request().get(ConnectionOverviewInfo.class);

        Comparator<TaskCounterInfo> counterInfoComparator = new Comparator<TaskCounterInfo>() {
            @Override
            public int compare(TaskCounterInfo o1, TaskCounterInfo o2) {
                return Long.valueOf(o2.count).compareTo(o1.count);
            }
        };
        assertThat(connectionOverviewInfo.connectionSummary.counters).hasSize(3);
        assertThat(connectionOverviewInfo.overviews.get(0).counters).isSortedAccordingTo(counterInfoComparator);
        assertThat(connectionOverviewInfo.overviews.get(1).counters).isSortedAccordingTo(counterInfoComparator);

        Comparator<TaskBreakdownInfo> taskBreakdownInfoComparator = new Comparator<TaskBreakdownInfo>() {
            @Override
            public int compare(TaskBreakdownInfo o1, TaskBreakdownInfo o2) {
                return Long.valueOf(o2.failedCount).compareTo(o1.failedCount);
            }
        };
        assertThat(connectionOverviewInfo.breakdowns.get(0).counters).isSortedAccordingTo(taskBreakdownInfoComparator);
        assertThat(connectionOverviewInfo.breakdowns.get(1).counters).isSortedAccordingTo(taskBreakdownInfoComparator);
        assertThat(connectionOverviewInfo.breakdowns.get(2).counters).isSortedAccordingTo(taskBreakdownInfoComparator);
    }

    private ConnectionTaskDeviceTypeHeatMap createDeviceTypeHeatMap() {
        ConnectionTaskDeviceTypeHeatMap heatMap = mock(ConnectionTaskDeviceTypeHeatMap.class);
        List<ConnectionTaskHeatMapRow<DeviceType>> rows = new ArrayList<>();
        ComSessionSuccessIndicatorOverviewImpl counters = new ComSessionSuccessIndicatorOverviewImpl(103L);
        counters.add(createCounter(ComSession.SuccessIndicator.Broken, 100L));
        counters.add(createCounter(ComSession.SuccessIndicator.SetupError, 101L));
        counters.add(createCounter(ComSession.SuccessIndicator.Success, 102L));
        long id=1;
        for (String name: Arrays.asList("deviceType1", "deviceType2", "deviceType3")) {
            DeviceType deviceType = mock(DeviceType.class);
            when(deviceType.getName()).thenReturn(name);
            when(deviceType.getId()).thenReturn(id++);
            rows.add(new ConnectionTaskHeatMapRowImpl<>(deviceType, counters));
        }
        when(heatMap.iterator()).thenReturn(rows.iterator());
        return heatMap;
    }

    private DeviceTypeBreakdown createDeviceTypeBreakdown() {
        DeviceTypeBreakdown mock = mock(DeviceTypeBreakdown.class);
        when(mock.iterator()).thenReturn(Collections.<TaskStatusBreakdownCounter<DeviceType>>emptyIterator());
        return mock;
    }

    private ConnectionTypeBreakdown createConnectionTypeBreakdown() {
        ConnectionTypeBreakdown mock = mock(ConnectionTypeBreakdown.class);
        when(mock.iterator()).thenReturn(Collections.<TaskStatusBreakdownCounter<ConnectionTypePluggableClass>>emptyIterator());
        return mock;
    }

    private ComPortPoolBreakdown createComPortPoolBreakdown() {
        ComPortPoolBreakdown mock = mock(ComPortPoolBreakdown.class);
        when(mock.getTotalCount()).thenReturn((long) (234+4+411));
        when(mock.getTotalFailedCount()).thenReturn(234L);
        when(mock.getTotalPendingCount()).thenReturn(4L);
        when(mock.getTotalSuccessCount()).thenReturn(411L);
        List<TaskStatusBreakdownCounter<ComPortPool>> counters = new ArrayList<>();
        counters.add(new TaskStatusBreakdownCounterImpl<>(mockComPortPool(1, "Outbound IP"), 11L, 25L, 11L));
        counters.add(new TaskStatusBreakdownCounterImpl<>(mockComPortPool(2, "Outbound IP post dial"), 411L, 233L, 78L));
        counters.add(new TaskStatusBreakdownCounterImpl<>(mockComPortPool(3, "Outbound UDP"), 11L, 1233L, 8L));
        counters.add(new TaskStatusBreakdownCounterImpl<>(mockComPortPool(4, "Serial"), 911L, 0L, 8L));
        counters.add(new TaskStatusBreakdownCounterImpl<>(mockComPortPool(5, "Serial PEMP"), 1L, 0L, 8L));
        counters.add(new TaskStatusBreakdownCounterImpl<>(mockComPortPool(6, "Serial PTPP"), 36L, 0L, 0L));
        counters.add(new TaskStatusBreakdownCounterImpl<>(mockComPortPool(7, "GPRS"), 1036L, 29L, 0L));
        when(mock.iterator()).thenReturn(counters.iterator());
        return mock;
    }

    private ComPortPool mockComPortPool(long id, String name) {
        ComPortPool mock = mock(ComPortPool.class);
        when(mock.getName()).thenReturn(name);
        when(mock.getId()).thenReturn(id);
        return mock;
    }


}