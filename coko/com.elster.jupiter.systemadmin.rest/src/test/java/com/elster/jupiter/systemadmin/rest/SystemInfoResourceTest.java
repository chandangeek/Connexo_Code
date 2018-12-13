/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.systemadmin.rest.imp.response.SystemInfo;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class SystemInfoResourceTest extends SystemApplicationJerseyTest {

    @Test
    public void testGetSystemInformation() {
        when(bundleContext.getProperty(BootstrapService.JDBC_DRIVER_URL)).thenReturn("url");
        when(bundleContext.getProperty(BootstrapService.JDBC_USER)).thenReturn("user");

        Response response = target("/systeminfo").request().get();

        SystemInfo systemInfo = response.readEntity(SystemInfo.class);
        assertThat(systemInfo.jre).isNotNull();
        assertThat(systemInfo.jvm).isNotNull();
        assertThat(systemInfo.javaHome).isNotNull();
        assertThat(systemInfo.javaClassPath).isNotNull();
        assertThat(systemInfo.osName).isNotNull();
        assertThat(systemInfo.osArch).isNotNull();
        assertThat(systemInfo.timeZone).isNotNull();
        assertThat(systemInfo.numberOfProcessors).isNotNull();
        assertThat(systemInfo.totalMemory).isNotNull();
        assertThat(systemInfo.freeMemory).isNotNull();
        assertThat(systemInfo.usedMemory).isNotNull();
        assertThat(systemInfo.lastStartedTime).isNotNull();
        assertThat(systemInfo.serverUptime).isNotNull();
        assertThat(systemInfo.dbConnectionUrl).isEqualTo("url");
        assertThat(systemInfo.dbUser).isEqualTo("user");
        assertThat(systemInfo.dbMaxConnectionsNumber).isEqualTo(BootstrapService.JDBC_POOLMAXLIMIT_DEFAULT);
        assertThat(systemInfo.dbMaxStatementsPerRequest).isEqualTo(BootstrapService.JDBC_POOLMAXSTATEMENTS_DEFAULT);
    }
}
