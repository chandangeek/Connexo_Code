/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.domain.util.Finder;
import com.energyict.mdc.dashboard.rest.status.ComServerStatusInfo;
import com.energyict.mdc.dashboard.rest.status.ComServerStatusSummaryInfo;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.status.ComServerStatus;
import com.energyict.mdc.engine.status.ComServerType;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.dashboard.rest.status.ComServerStatusSummaryResource} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-23 (08:47)
 */
public class ComServerStatusSummaryResourceTest extends DashboardApplicationJerseyTest {

    @Test
    public void testNoServersConfigured() {
        Finder<ComServer> allComServers = mock(Finder.class);
        when(allComServers.find()).thenReturn(Collections.emptyList());
        when(this.engineConfigurationService.findAllComServers()).thenReturn(allComServers);

        // Business method
        ComServerStatusSummaryInfo summaryInfo = target("/comserverstatussummary").request().get(ComServerStatusSummaryInfo.class);

        // Asserts
        assertThat(summaryInfo.comServerStatusInfos).isEmpty();
    }

    @Test
    public void testServerNotRunning () {
        long comServerId = 1L;
        String comServerName = "testServerNotRunning";
        OnlineComServer comServer = mock(OnlineComServer.class);
        when(comServer.getId()).thenReturn(comServerId);
        when(comServer.getName()).thenReturn(comServerName);
        when(comServer.isActive()).thenReturn(true);
        when(comServer.getStatusUri()).thenReturn("http://localhost:9998");
        when(this.engineConfigurationService.findAllOnlineComServers()).thenReturn(Arrays.asList(comServer));

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
        assertThat(comServerStatusInfo.comServerId).isEqualTo(comServerId);
        assertThat(comServerStatusInfo.comServerName).isEqualTo(comServerName);
        assertThat(comServerStatusInfo.comServerType).isEqualTo("Online");
        assertThat(comServerStatusInfo.running).isFalse();
        assertThat(comServerStatusInfo.blocked).isFalse();
    }

}