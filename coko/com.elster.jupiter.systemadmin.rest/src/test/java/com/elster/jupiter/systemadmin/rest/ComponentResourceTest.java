/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.system.BundleType;
import com.elster.jupiter.system.Component;
import com.elster.jupiter.system.ComponentStatus;
import com.elster.jupiter.system.RuntimeComponent;
import com.elster.jupiter.system.Subsystem;
import com.elster.jupiter.systemadmin.rest.imp.resource.BundleTypeTranslationKeys;

import com.jayway.jsonpath.JsonModel;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ComponentResourceTest extends SystemApplicationJerseyTest {

    private static final String PLATFORM = "platform";
    private static final String MULTISENSE = "multisense";

    @Before
    public void setUp() throws Exception {
        super.setUp();
        RuntimeComponent rc1 = mockRuntimeComponent(1L, "a", PLATFORM, BundleType.THIRD_PARTY);
        RuntimeComponent rc2 = mockRuntimeComponent(2L, "b", MULTISENSE, BundleType.THIRD_PARTY);
        RuntimeComponent rc3 = mockRuntimeComponent(3L, "c", MULTISENSE, BundleType.THIRD_PARTY);
        RuntimeComponent rc4 = mockRuntimeComponent(4L, "a", MULTISENSE, BundleType.APPLICATION_SPECIFIC);
        RuntimeComponent rc5 = mockRuntimeComponent(5L, "b", PLATFORM, BundleType.APPLICATION_SPECIFIC);
        when(subsystemService.getRuntimeComponents()).thenReturn(Arrays.asList(rc1, rc2, rc3, rc4, rc5));
    }

    @Override
    protected void setupThesaurus() {
        NlsMessageFormat applicationSpecific = mock(NlsMessageFormat.class);
        when(applicationSpecific.format(anyVararg())).thenReturn("Application-specific");
        doReturn(applicationSpecific).when(thesaurus).getFormat(BundleTypeTranslationKeys.APPLICATION_SPECIFIC);
        NlsMessageFormat thirdParty = mock(NlsMessageFormat.class);
        when(thirdParty.format(anyVararg())).thenReturn("Third party");
        doReturn(thirdParty).when(thesaurus).getFormat(BundleTypeTranslationKeys.THIRD_PARTY);
    }

    @Test
    public void testGetComponentsList() {
        String response = target("/components").request().get(String.class);

        JsonModel model = JsonModel.create(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(5);
        assertThat(model.<List>get("$.components").size()).isEqualTo(5);

        assertThat(model.<Number>get("$.components[0].bundleId")).isEqualTo(4);
        assertThat(model.<String>get("$.components[0].application")).isEqualTo(MULTISENSE);
        assertThat(model.<String>get("$.components[0].bundleType")).isEqualTo("Application-specific");
        assertThat(model.<String>get("$.components[0].name")).isEqualTo("a");
        assertThat(model.<String>get("$.components[0].status")).isEqualTo("Active");
        assertThat(model.<String>get("$.components[0].version")).isEqualTo("1.0.0");

        assertThat(model.<Number>get("$.components[1].bundleId")).isEqualTo(2);
        assertThat(model.<String>get("$.components[1].application")).isEqualTo(MULTISENSE);
        assertThat(model.<String>get("$.components[1].bundleType")).isEqualTo("Third party");
        assertThat(model.<String>get("$.components[1].name")).isEqualTo("b");
        assertThat(model.<String>get("$.components[1].status")).isEqualTo("Active");
        assertThat(model.<String>get("$.components[1].version")).isEqualTo("1.0.0");

        assertThat(model.<Number>get("$.components[2].bundleId")).isEqualTo(3);
        assertThat(model.<String>get("$.components[2].application")).isEqualTo(MULTISENSE);
        assertThat(model.<String>get("$.components[2].bundleType")).isEqualTo("Third party");
        assertThat(model.<String>get("$.components[2].name")).isEqualTo("c");
        assertThat(model.<String>get("$.components[2].status")).isEqualTo("Active");
        assertThat(model.<String>get("$.components[2].version")).isEqualTo("1.0.0");

        assertThat(model.<Number>get("$.components[3].bundleId")).isEqualTo(5);
        assertThat(model.<String>get("$.components[3].application")).isEqualTo(PLATFORM);
        assertThat(model.<String>get("$.components[3].bundleType")).isEqualTo("Application-specific");
        assertThat(model.<String>get("$.components[3].name")).isEqualTo("b");
        assertThat(model.<String>get("$.components[3].status")).isEqualTo("Active");
        assertThat(model.<String>get("$.components[3].version")).isEqualTo("1.0.0");

        assertThat(model.<Number>get("$.components[4].bundleId")).isEqualTo(1);
        assertThat(model.<String>get("$.components[4].application")).isEqualTo(PLATFORM);
        assertThat(model.<String>get("$.components[4].bundleType")).isEqualTo("Third party");
        assertThat(model.<String>get("$.components[4].name")).isEqualTo("a");
        assertThat(model.<String>get("$.components[4].status")).isEqualTo("Active");
        assertThat(model.<String>get("$.components[4].version")).isEqualTo("1.0.0");
    }

    @Test
    public void testGetComponentsListFilterByApplication() throws UnsupportedEncodingException {
        String response = target("/components").queryParam("filter", ExtjsFilter.filter("application", Collections.singletonList(PLATFORM))).request().get(String.class);

        JsonModel model = JsonModel.create(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(2);
        assertThat(model.<List>get("$.components").size()).isEqualTo(2);

        assertThat(model.<List<Number>>get("$.components[*].bundleId")).containsExactly(5, 1);
    }

    @Test
    public void testGetComponentsListFilterByBundleType() throws UnsupportedEncodingException {
        String response = target("/components").queryParam("filter", ExtjsFilter.filter("bundleType", Collections.singletonList("thirdParty"))).request().get(String.class);

        JsonModel model = JsonModel.create(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(3);
        assertThat(model.<List>get("$.components").size()).isEqualTo(3);

        assertThat(model.<List<Number>>get("$.components[*].bundleId")).containsExactly(2, 3, 1);
    }

    @Test
    public void testGetComponentsListFilterByStatus() throws UnsupportedEncodingException {
        String response = target("/components").queryParam("filter", ExtjsFilter.filter("status", Collections.singletonList("inactive"))).request().get(String.class);

        JsonModel model = JsonModel.create(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(0);
        assertThat(model.<List>get("$.components").size()).isEqualTo(0);
    }

    private RuntimeComponent mockRuntimeComponent(long bundleId, String name, String application, BundleType bundleType) {
        RuntimeComponent runtimeComponent = mock(RuntimeComponent.class);
        when(runtimeComponent.getId()).thenReturn(bundleId);
        when(runtimeComponent.getName()).thenReturn(name);
        when(runtimeComponent.getStatus()).thenReturn(ComponentStatus.ACTIVE);
        Subsystem subsystem = mock(Subsystem.class);
        when(runtimeComponent.getSubsystem()).thenReturn(subsystem);
        when(subsystem.getId()).thenReturn(application);
        when(subsystem.getName()).thenReturn(application);
        Component component = mock(Component.class);
        when(runtimeComponent.getComponent()).thenReturn(component);
        when(component.getBundleType()).thenReturn(bundleType);
        when(component.getVersion()).thenReturn("1.0.0");
        return runtimeComponent;
    }
}
