package com.energyict.mdc.dashboard.rest.status;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.dashboard.ComTaskCompletionOverview;
import com.energyict.mdc.dashboard.ConnectionStatusOverview;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.engine.status.StatusService;
import com.energyict.mdc.tasks.history.CompletionCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private static final String DUMMY_THESAURUS_STRING = "";

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
                    if (messageSeeds.getKey().equals((String) invocationOnMock.getArguments()[0])) {
                        return messageSeeds.getDefaultFormat();
                    }
                }
                return "xxx";
            }
        });
        NlsMessageFormat mft = mock(NlsMessageFormat.class);
        when(mft.format(any(Object[].class))).thenReturn("format");
        when(thesaurus.getFormat(Matchers.<MessageSeed>anyObject())).thenReturn(mft);

        ConnectionStatusOverview connectionStatusOverview = createConnectionStatusOverview();
        when(dashboardService.getConnectionStatusOverview()).thenReturn(connectionStatusOverview);
        ComTaskCompletionOverview comTaskCompletionOverview = createComTaskCompletionOverview();
        when(dashboardService.getComTaskCompletionOverview()).thenReturn(comTaskCompletionOverview);
    }

    private ComTaskCompletionOverview createComTaskCompletionOverview() {
        ComTaskCompletionOverview overview = mock(ComTaskCompletionOverview.class);
        when(overview.getTotalCount()).thenReturn(100L);
        List<Counter<CompletionCode>> counters = new ArrayList<>();
        counters.add(createCounter(CompletionCode.Ok, 101L));
        counters.add(createCounter(CompletionCode.ProtocolError, 12L));
        counters.add(createCounter(CompletionCode.ConnectionError, 41L));
        counters.add(createCounter(CompletionCode.IOError, 1L));
        when(overview.iterator()).thenReturn(counters.iterator());
        return overview;
    }

    private ConnectionStatusOverview createConnectionStatusOverview() {
        ConnectionStatusOverview overview = mock(ConnectionStatusOverview.class);
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
    public void testGetOverview() {
        Map<String, Object> statusInfo = target("/connectionoverview").request().get(Map.class);

    }

}