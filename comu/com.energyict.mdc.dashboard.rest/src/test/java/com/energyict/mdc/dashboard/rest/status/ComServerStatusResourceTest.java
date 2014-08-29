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
import com.energyict.mdc.engine.status.ComServerStatus;
import com.energyict.mdc.engine.status.ComServerType;
import com.energyict.mdc.engine.status.StatusService;
import java.util.Date;
import java.util.Map;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ComServerStatusResource} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (17:27)
 */
public class ComServerStatusResourceTest extends JerseyTest {

    private static final String DUMMY_THESAURUS_STRING = "";

    @Mock
    private StatusService statusService;
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
        MockitoAnnotations.initMocks(this);
        ResourceConfig resourceConfig = new ResourceConfig(
                ComServerStatusResource.class,
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
    public void testServerNotRunning () {
        ComServerStatus notRunning = mock(ComServerStatus.class);
        when(notRunning.isRunning()).thenReturn(false);
        when(notRunning.isBlocked()).thenReturn(false);
        when(notRunning.getBlockTime()).thenReturn(null);
        when(notRunning.getComServerName()).thenReturn("testServerNotRunning");
        when(notRunning.getComServerType()).thenReturn(ComServerType.ONLINE);
        when(this.statusService.getStatus()).thenReturn(notRunning);

        // Business method
        Map<String, Object> statusInfo = target("/comserverstatus").request().get(Map.class);

        // Asserts
        assertThat(statusInfo).containsKey("comServerName");
        assertThat(statusInfo).containsKey("comServerType");
        assertThat(statusInfo).containsKey("running");
        assertThat(statusInfo).containsKey("blocked");
    }

    @Test
    public void testServerRunningButNotBlocked () {
        ComServerStatus notRunning = mock(ComServerStatus.class);
        when(notRunning.isRunning()).thenReturn(true);
        when(notRunning.isBlocked()).thenReturn(false);
        when(notRunning.getBlockTime()).thenReturn(null);
        when(notRunning.getComServerName()).thenReturn("testServerRunningButNotBlocked");
        when(notRunning.getComServerType()).thenReturn(ComServerType.ONLINE);
        when(this.statusService.getStatus()).thenReturn(notRunning);

        // Business method
        Map<String, Object> statusInfo = target("/comserverstatus").request().get(Map.class);

        // Asserts
        assertThat(statusInfo).containsKey("comServerName");
        assertThat(statusInfo).containsKey("comServerType");
        assertThat(statusInfo).containsKey("running");
        assertThat(statusInfo).containsKey("blocked");
    }

    @Test
    public void testServerRunningAndBlocked () {
        ComServerStatus notRunning = mock(ComServerStatus.class);
        when(notRunning.isRunning()).thenReturn(true);
        when(notRunning.isBlocked()).thenReturn(true);
        when(notRunning.getBlockTime()).thenReturn(new Duration(DateTimeConstants.MILLIS_PER_MINUTE * 5));
        when(notRunning.getBlockTimestamp()).thenReturn(new Date());
        when(notRunning.getComServerName()).thenReturn("testServerRunningAndBlocked");
        when(notRunning.getComServerType()).thenReturn(ComServerType.ONLINE);
        when(this.statusService.getStatus()).thenReturn(notRunning);

        // Business method
        Map<String, Object> statusInfo = target("/comserverstatus").request().get(Map.class);

        // Asserts
        assertThat(statusInfo).containsKey("comServerName");
        assertThat(statusInfo).containsKey("comServerType");
        assertThat(statusInfo).containsKey("running");
        assertThat(statusInfo).containsKey("blocked");
        assertThat(statusInfo).containsKey("blockTime");
        assertThat(statusInfo).containsKey("blockedSince");
    }

}