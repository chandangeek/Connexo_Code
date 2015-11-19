package com.elster.jupiter.systemadmin.rest;

import com.elster.jupiter.system.Subsystem;
import com.elster.jupiter.system.beans.SubsystemImpl;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class FieldResourceTest extends SystemApplicationJerseyTest {

    @Test
    public void testGetApplications() {
        Subsystem s1 = new SubsystemImpl("Pulse", "Connexo Pulse", "1.0.0");
        Subsystem s2 = new SubsystemImpl("MultiSense", "Connexo Multisense", "1.1.0");
        when(subsystemService.getSubsystems()).thenReturn(Arrays.asList(s1, s2));

        String response = target("/fields/applications").request().get(String.class);

        JsonModel model = JsonModel.create(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(2);
        assertThat(model.<List>get("$.applications").size()).isEqualTo(2);

        assertThat(model.<String>get("$.applications[0].id")).isEqualTo("MultiSense");
        assertThat(model.<String>get("$.applications[0].name")).isEqualTo("Connexo Multisense");
        assertThat(model.<String>get("$.applications[0].version")).isEqualTo("1.1.0");

        assertThat(model.<String>get("$.applications[1].id")).isEqualTo("Pulse");
        assertThat(model.<String>get("$.applications[1].name")).isEqualTo("Connexo Pulse");
        assertThat(model.<String>get("$.applications[1].version")).isEqualTo("1.0.0");
    }

    @Test
    public void testGetBundleTypes() {
        String response = target("/fields/bundleTypes").request().get(String.class);

        JsonModel model = JsonModel.create(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(2);
        assertThat(model.<List>get("$.bundleTypes").size()).isEqualTo(2);

        assertThat(model.<String>get("$.bundleTypes[0].id")).isEqualTo("appSpecific");
        assertThat(model.<String>get("$.bundleTypes[0].name")).isEqualTo("Application-specific");

        assertThat(model.<String>get("$.bundleTypes[1].id")).isEqualTo("thirdParty");
        assertThat(model.<String>get("$.bundleTypes[1].name")).isEqualTo("Third party");
    }

    @Test
    public void testGetComponentStatuses() {
        String response = target("/fields/componentStatuses").request().get(String.class);

        JsonModel model = JsonModel.create(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(6);
        assertThat(model.<List>get("$.componentStatuses").size()).isEqualTo(6);

        assertThat(model.<List<String>>get("$.componentStatuses[*].id"))
                .containsExactly("active", "installed", "resolved", "starting", "stopping", "uninstalled");
        assertThat(model.<List<String>>get("$.componentStatuses[*].name"))
                .containsExactly("Active", "Installed", "Resolved", "Starting", "Stopping", "Uninstalled");
    }
}
