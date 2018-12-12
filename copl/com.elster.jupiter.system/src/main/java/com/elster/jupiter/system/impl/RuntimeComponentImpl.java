/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.system.impl;

import com.elster.jupiter.system.Component;
import com.elster.jupiter.system.ComponentStatus;
import com.elster.jupiter.system.RuntimeComponent;
import com.elster.jupiter.system.Subsystem;
import com.elster.jupiter.system.utils.OsgiUtils;
import org.osgi.framework.Bundle;

public class RuntimeComponentImpl implements RuntimeComponent {

    private long id;
    private String name;
    private ComponentStatus status;
    private Component component;
    private Subsystem subsystem;

    public RuntimeComponentImpl(Bundle bundle, Component component, Subsystem subsystem) {
        this.id = bundle.getBundleId();
        this.name = buildBundleName(bundle);
        this.status = OsgiUtils.bundleStateToComponentStatus(bundle.getState());
        this.component = component;
        this.subsystem = subsystem;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ComponentStatus getStatus() {
        return status;
    }

    @Override
    public Component getComponent() {
        return component;
    }

    @Override
    public Subsystem getSubsystem() {
        return subsystem;
    }

    private String buildBundleName(Bundle bundle) {
        StringBuilder builder = new StringBuilder()
                .append(bundle.getHeaders().get("Bundle-Name"))
                .append(" (")
                .append(bundle.getSymbolicName())
                .append(")");
        return builder.toString();
    }
}
