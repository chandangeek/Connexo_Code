package com.elster.jupiter.subsystem.kore.impl;

import com.elster.jupiter.system.SubsystemService;
import com.elster.jupiter.system.beans.SubsystemImpl;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.subsystem.kore", immediate = true)
public class KoreSubsystemActivator {

    public static final String PLATFORM_ID = "Platform";
    public static final String PLATFORM_NAME = "Connexo Platform";

    private volatile SubsystemService subsystemService;

    public KoreSubsystemActivator() {
    }

    @Activate
    public void activate(BundleContext context) {
        SubsystemImpl subsystem = new SubsystemImpl(PLATFORM_ID, PLATFORM_NAME, context.getBundle().getVersion().toString());
        this.subsystemService.registerSubsystem(subsystem);
    }

    @Reference
    public void setSubsystemService(SubsystemService subsystemService) {
        this.subsystemService = subsystemService;
    }
}
