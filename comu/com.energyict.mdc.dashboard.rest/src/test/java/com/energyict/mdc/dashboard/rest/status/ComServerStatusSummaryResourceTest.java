package com.energyict.mdc.dashboard.rest.status;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.RemoteComServer;
import com.energyict.mdc.engine.status.ComServerStatus;
import com.energyict.mdc.engine.status.ComServerType;
import com.energyict.mdc.engine.status.StatusService;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.util.exception.MessageSeed;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;

import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.Collections;

import org.junit.*;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ComServerStatusSummaryResource} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-23 (08:47)
 */
public class ComServerStatusSummaryResourceTest extends JerseyTest {

    private static final String DUMMY_THESAURUS_STRING = "";

    @Mock
    private StatusService statusService;
    @Mock
    private EngineModelService engineModelService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void setupMocks () {
        when(thesaurus.getString(anyString(), anyString())).thenReturn(DUMMY_THESAURUS_STRING);
        NlsMessageFormat mft = mock(NlsMessageFormat.class);
        when(mft.format(any(Object[].class))).thenReturn("format");
        when(thesaurus.getFormat(Matchers.<MessageSeed>anyObject())).thenReturn(mft);
    }

    @Override
    protected Application configure() {
        // Enable parallel runs
        // forceSet(TestProperties.CONTAINER_PORT, "0");
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        MockitoAnnotations.initMocks(this);
        ResourceConfig resourceConfig = new ResourceConfig(
                ComServerStatusResource.class,
                ComServerStatusSummaryResource.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedFieldValidationExceptionMapper.class,
                LocalizedExceptionMapper.class);
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(statusService).to(StatusService.class);
                bind(engineModelService).to(EngineModelService.class);
                bind(nlsService).to(NlsService.class);
                bind(thesaurus).to(Thesaurus.class);
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
    public void testNoServersConfigured() {
        Finder allComServers = mock(Finder.class);
        when(allComServers.find()).thenReturn(Collections.emptyList());
        when(this.engineModelService.findAllComServers()).thenReturn(allComServers);

        // Business method
        ComServerStatusSummaryInfo summaryInfo = target("/comserverstatussummary").request().get(ComServerStatusSummaryInfo.class);

        // Asserts
        assertThat(summaryInfo.comServerStatusInfos).isEmpty();
    }

    @Test
    public void testServerNotRunning () {
        String comServerName = "testServerNotRunning";
        OnlineComServer comServer = mock(OnlineComServer.class);
        when(comServer.getName()).thenReturn(comServerName);
        when(comServer.getStatusUri()).thenReturn("http://localhost:9998");
        when(this.engineModelService.findAllOnlineComServers()).thenReturn(Arrays.asList(comServer));

        ComServerStatus notRunning = mock(ComServerStatus.class);
        when(notRunning.isRunning()).thenReturn(false);
        when(notRunning.isBlocked()).thenReturn(false);
        when(notRunning.getBlockTime()).thenReturn(null);
        when(notRunning.getComServerName()).thenReturn(comServerName);
        when(notRunning.getComServerType()).thenReturn(ComServerType.ONLINE);
        when(this.statusService.getStatus()).thenReturn(notRunning);

        // Business method
        ComServerStatusSummaryInfo summaryInfo = target("/comserverstatussummary").request().get(ComServerStatusSummaryInfo.class);

        // Asserts
        assertThat(summaryInfo.comServerStatusInfos).hasSize(1);
        ComServerStatusInfo comServerStatusInfo = summaryInfo.comServerStatusInfos.get(0);
        assertThat(comServerStatusInfo).isNotNull();
        assertThat(comServerStatusInfo.comServerName).isEqualTo(comServerName);
        assertThat(comServerStatusInfo.comServerType).isEqualTo(ComServerType.ONLINE);
        assertThat(comServerStatusInfo.running).isFalse();
        assertThat(comServerStatusInfo.blocked).isFalse();
    }

    @Test
    public void testOnlineServerRunningButNotBlocked () {
        String comServerName = "testOnlineServerRunningButNotBlocked";
        OnlineComServer comServer = mock(OnlineComServer.class);
        when(comServer.getName()).thenReturn(comServerName);
        when(comServer.getStatusUri()).thenReturn("http://localhost:9998");
        when(this.engineModelService.findAllOnlineComServers()).thenReturn(Arrays.asList(comServer));

        this.testServerRunningButNotBlocked(comServer, ComServerType.ONLINE);
    }

    @Test
    public void testRemoteServerRunningButNotBlocked () {
        String comServerName = "testRemoteServerRunningButNotBlocked";
        RemoteComServer comServer = mock(RemoteComServer.class);
        when(comServer.getName()).thenReturn(comServerName);
        when(comServer.getStatusUri()).thenReturn("http://localhost:9998");
        when(this.engineModelService.findAllRemoteComServers()).thenReturn(Arrays.asList(comServer));

        this.testServerRunningButNotBlocked(comServer, ComServerType.REMOTE);
    }

    /**
     * Tests that the {@link ComServer} that is already setup
     * in the {@link EngineModelService} is running but not blocked.
     * @param comServer The ComServer
     * @param comServerType The type of ComServer
     */
    private void testServerRunningButNotBlocked(ComServer comServer, ComServerType comServerType) {
        String comServerName = comServer.getName();

        ComServerStatus runningButNotBlocked = mock(ComServerStatus.class);
        when(runningButNotBlocked.isRunning()).thenReturn(true);
        when(runningButNotBlocked.isBlocked()).thenReturn(false);
        when(runningButNotBlocked.getBlockTime()).thenReturn(null);
        when(runningButNotBlocked.getComServerName()).thenReturn(comServerName);
        when(runningButNotBlocked.getComServerType()).thenReturn(comServerType);
        when(this.statusService.getStatus()).thenReturn(runningButNotBlocked);

        // Business method
        ComServerStatusSummaryInfo summaryInfo = target("/comserverstatussummary").request().get(ComServerStatusSummaryInfo.class);

        // Asserts
        assertThat(summaryInfo.comServerStatusInfos).hasSize(1);
        ComServerStatusInfo comServerStatusInfo = summaryInfo.comServerStatusInfos.get(0);
        assertThat(comServerStatusInfo).isNotNull();
        assertThat(comServerStatusInfo.comServerName).isEqualTo(comServerName);
        assertThat(comServerStatusInfo.comServerType).isEqualTo(comServerType);
        assertThat(comServerStatusInfo.running).isTrue();
        assertThat(comServerStatusInfo.blocked).isFalse();
    }

    @Test
    public void testOnlineServerRunningAndBlocked () {
        String comServerName = "testServerRunningButNotBlocked";
        OnlineComServer comServer = mock(OnlineComServer.class);
        when(comServer.getName()).thenReturn(comServerName);
        when(comServer.getStatusUri()).thenReturn("http://localhost:9998");
        when(this.engineModelService.findAllOnlineComServers()).thenReturn(Arrays.asList(comServer));

        this.testServerRunningAndBlocked(comServer, ComServerType.ONLINE);
    }

    @Test
    public void testRemoteServerRunningAndBlocked () {
        String comServerName = "testServerRunningButNotBlocked";
        RemoteComServer comServer = mock(RemoteComServer.class);
        when(comServer.getName()).thenReturn(comServerName);
        when(comServer.getStatusUri()).thenReturn("http://localhost:9998");
        when(this.engineModelService.findAllRemoteComServers()).thenReturn(Arrays.asList(comServer));

        this.testServerRunningAndBlocked(comServer, ComServerType.REMOTE);
    }

    /**
     * Tests that the {@link ComServer} that is already setup
     * in the {@link EngineModelService} is running and blocked.
     *
     * @param comServer The ComServer
     * @param comServerType The type of ComServer
     */
    private void testServerRunningAndBlocked(ComServer comServer, ComServerType comServerType) {
        String comServerName = comServer.getName();
        ComServerStatus runningAndBlocked = mock(ComServerStatus.class);
        when(runningAndBlocked.isRunning()).thenReturn(true);
        when(runningAndBlocked.isBlocked()).thenReturn(true);
        when(runningAndBlocked.getBlockTime()).thenReturn(new Duration(DateTimeConstants.MILLIS_PER_MINUTE * 5));
        when(runningAndBlocked.getComServerName()).thenReturn(comServerName);
        when(runningAndBlocked.getComServerType()).thenReturn(comServerType);
        when(this.statusService.getStatus()).thenReturn(runningAndBlocked);

        // Business method
        ComServerStatusSummaryInfo summaryInfo = target("/comserverstatussummary").request().get(ComServerStatusSummaryInfo.class);

        // Asserts
        assertThat(summaryInfo.comServerStatusInfos).hasSize(1);
        ComServerStatusInfo comServerStatusInfo = summaryInfo.comServerStatusInfos.get(0);
        assertThat(comServerStatusInfo).isNotNull();
        assertThat(comServerStatusInfo.comServerName).isEqualTo(comServerName);
        assertThat(comServerStatusInfo.comServerType).isEqualTo(comServerType);
        assertThat(comServerStatusInfo.running).isTrue();
        assertThat(comServerStatusInfo.blocked).isTrue();
        assertThat(comServerStatusInfo.blockTime).isEqualTo(TimeDuration.minutes(5));
    }

}