package com.elster.jupiter.systemadmin.rest;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.system.BundleType;
import com.elster.jupiter.system.ComponentStatus;
import com.elster.jupiter.system.RuntimeComponent;
import com.elster.jupiter.system.beans.ComponentImpl;
import com.elster.jupiter.system.beans.SubsystemImpl;
import com.elster.jupiter.systemadmin.rest.imp.response.SystemInfo;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;

import javax.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeploymentInfoTest extends SystemApplicationJerseyTest {

    @Test
    public void testGetSystemInformation() {
        when(bundleContext.getProperty(BootstrapService.JDBC_DRIVER_URL)).thenReturn("com.elster.jupiter.datasource.jdbcurl");
        when(bundleContext.getProperty(BootstrapService.JDBC_USER)).thenReturn("com.elster.jupiter.datasource.jdbcuser");
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
        assertThat(systemInfo.dbConnectionUrl).isNotNull();
        assertThat(systemInfo.dbUser).isNotNull();
        assertThat(systemInfo.dbMaxConnectionsNumber).isNotNull();
        assertThat(systemInfo.dbMaxStatementsPerRequest).isNotNull();
    }

    @Test
    public void testGetApplicationInformation() {
        SubsystemImpl subsystem = new SubsystemImpl("Pulse", "Connexo Pulse", "1.0");
        when(subsystemService.getSubsystems()).thenReturn(Collections.singletonList(subsystem));
        String response = target("/fields/applications").request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Number> get("$.total")).isEqualTo(1);
        assertThat(model.<List> get("$.applications").size()).isEqualTo(1);
        assertThat(model.<String> get("$.applications[0].id")).isEqualTo("Pulse");
        assertThat(model.<String> get("$.applications[0].name")).isEqualTo("Connexo Pulse");
        assertThat(model.<String> get("$.applications[0].version")).isEqualTo("1.0");
    }

    @Test
    public void testGetComponentsList() {
        String application = "Connexo Pulse";
        String version = "1.0.0";
        String bundleName = "com.elster.jupiter.validation";
        SubsystemImpl subsystem = new SubsystemImpl("Pulse", application, "1.0");
        ComponentImpl component = new ComponentImpl("validation", bundleName, version, BundleType.APPLICATION_SPECIFIC, subsystem);
        RuntimeComponent runtimeComponent = new RuntimeComponent(1, bundleName, ComponentStatus.ACTIVE, component);
        when(subsystemService.getComponents(bundleContext)).thenReturn(Collections.singletonList(runtimeComponent));
        String response = target("/components").request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Number> get("$.total")).isEqualTo(1);
        assertThat(model.<List> get("$.components").size()).isEqualTo(1);
        assertThat(model.<String> get("$.components[0].id")).isEqualTo("1");
        assertThat(model.<String> get("$.components[0].application")).isEqualTo(application);
        assertThat(model.<String> get("$.components[0].bundleType")).isEqualTo("Application-specific");
        assertThat(model.<String> get("$.components[0].name")).isEqualTo(bundleName);
        assertThat(model.<String> get("$.components[0].version")).isEqualTo(version);
        assertThat(model.<String> get("$.components[0].status")).isEqualTo("Active");
    }
}
