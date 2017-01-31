/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.system;

import com.elster.jupiter.system.beans.ComponentImpl;
import com.elster.jupiter.system.beans.SubsystemImpl;
import com.elster.jupiter.system.impl.SubsystemServiceImpl;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SubsystemServiceTest {

    @Mock
    BundleContext bundleContext;

    SubsystemServiceImpl subsystemService;
    @Mock
    private UserService userService;

    @Before
    public void setUp() {
        subsystemService = new SubsystemServiceImpl();
        subsystemService.setUpgradeService(UpgradeModule.FakeUpgradeService.getInstance());
        subsystemService.setUserService(userService);
        subsystemService.activate(bundleContext);
    }

    @Test
    public void testRegisterSubsystem() {
        subsystemService.registerSubsystem(new SubsystemImpl("id", "name", "version"));

        List<Subsystem> subsystems = subsystemService.getSubsystems();

        assertThat(subsystems).hasSize(1);

        Subsystem subsystem = subsystems.get(0);
        assertThat(subsystem.getId()).isEqualTo("id");
        assertThat(subsystem.getName()).isEqualTo("name");
        assertThat(subsystem.getVersion()).isEqualTo("version");
    }

    @Test
    public void testUnregisterSubsystem() {
        SubsystemImpl subsystem = new SubsystemImpl("id", "name", "version");

        subsystemService.registerSubsystem(subsystem);

        assertThat(subsystemService.getSubsystems()).hasSize(1);

        subsystemService.unregisterSubsystem(subsystem);

        assertThat(subsystemService.getSubsystems()).isEmpty();
    }

    @Test
    public void testGetRuntimeComponents() {
        Bundle[] bundles = new Bundle[]{
                mockBundle("bundle1", "com.bundle1", "1.0.0"),
                mockBundle("bundle2", "com.bundle2", "2.0")
        };
        SubsystemImpl subsystem = new SubsystemImpl("app", "app", "1.10");
        subsystem.addComponents(Arrays.asList(mockComponent("com.bundle1", "1.0.0")));
        subsystemService.registerSubsystem(subsystem);
        when(bundleContext.getBundles()).thenReturn(bundles);

        List<RuntimeComponent> runtimeComponents = subsystemService.getRuntimeComponents();

        assertThat(runtimeComponents).hasSize(1);
        RuntimeComponent runtimeComponent = runtimeComponents.get(0);
        assertThat(runtimeComponent.getId()).isEqualTo(14L);
        assertThat(runtimeComponent.getName()).isEqualTo("bundle1 (com.bundle1)");
        assertThat(runtimeComponent.getStatus()).isEqualTo(ComponentStatus.RESOLVED);
        assertThat(runtimeComponent.getSubsystem().getName()).isEqualTo("app");
        assertThat(runtimeComponent.getComponent().getVersion()).isEqualTo("1.0.0");
    }

    private Component mockComponent(String name, String version) {
        ComponentImpl component = new ComponentImpl();
        component.setSymbolicName(name);
        component.setVersion(version);
        return component;
    }

    private Bundle mockBundle(String name, String symbolicName, String version) {
        Bundle bundle = mock(Bundle.class, RETURNS_DEEP_STUBS);
        when(bundle.getBundleId()).thenReturn(14L);
        when(bundle.getSymbolicName()).thenReturn(name);
        when(bundle.getVersion().toString()).thenReturn(version);
        when(bundle.getHeaders().get("Bundle-Name")).thenReturn(name);
        when(bundle.getHeaders().get("Bundle-Version")).thenReturn(version);
        when(bundle.getSymbolicName()).thenReturn(symbolicName);
        when(bundle.getState()).thenReturn(4);
        return bundle;
    }
}
