package com.elster.jupiter.subsystem.mdc.impl;

import com.elster.jupiter.license.License;
import com.elster.jupiter.system.SubsystemService;
import com.elster.jupiter.system.beans.SubsystemImpl;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.subsystem.mdc", immediate = true)
public class MdcSubsystemActivator {

    public static final String MULTISENSE_ID = "MultiSense";
    public static final String MULTISENSE_NAME = "Connexo MultiSense";
    public static final String APPLICATION_KEY = "MDC";

    private volatile License license;
    private volatile SubsystemService subsystemService;

    public MdcSubsystemActivator() {
    }

    @Activate
    public void activate(BundleContext context) {
        SubsystemImpl subsystem = new SubsystemImpl(MULTISENSE_ID, MULTISENSE_NAME, context.getBundle().getVersion().toString());
        this.subsystemService.registerSubsystem(subsystem);
    }

    @Reference
    public void setSubsystemService(SubsystemService subsystemService) {
        this.subsystemService = subsystemService;
    }

    @Reference(target = "(com.elster.jupiter.license.application.key=" + APPLICATION_KEY + ")")
    public void setLicense(License license) {
        this.license = license;
    }
}
