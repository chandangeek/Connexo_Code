/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.engine.status.ComServerStatus;
import com.energyict.mdc.engine.status.ComServerType;

import org.joda.time.DateTimeConstants;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.dashboard.rest.status.ComServerStatusResource} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (17:27)
 */
public class ComServerStatusResourceTest extends DashboardApplicationJerseyTest {

    @Test
    public void testServerNotRunning () {
        ComServerStatus notRunning = mock(ComServerStatus.class);
        when(notRunning.isRunning()).thenReturn(false);
        when(notRunning.isBlocked()).thenReturn(false);
        when(notRunning.getBlockTime()).thenReturn(null);
        when(notRunning.getComServerName()).thenReturn("testServerNotRunning");
        when(notRunning.getComServerType()).thenReturn(ComServerType.ONLINE);
        when(statusService.getStatus()).thenReturn(notRunning);

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
        when(notRunning.getBlockTime()).thenReturn(Duration.ofMillis(DateTimeConstants.MILLIS_PER_MINUTE * 5));
        when(notRunning.getBlockTimestamp()).thenReturn(Instant.now());
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