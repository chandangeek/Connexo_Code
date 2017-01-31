/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;

@Component(name = "com.elster.jupiter.metering.imports.impl.usagepoint.MeteringDataImporterContext", service = {MeteringDataImporterContext.class})
public class MeteringDataImporterContext {
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile UserService userService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile LicenseService licenseService;
    private volatile Clock clock;
    private volatile MetrologyConfigurationService metrologyConfigurationService;

    public MeteringDataImporterContext() {
    }

    @Inject
    public MeteringDataImporterContext(PropertySpecService propertySpecService,
                                       NlsService nlsService,
                                       MeteringService meteringService,
                                       UserService userService,
                                       ThreadPrincipalService threadPrincipalService,
                                       CustomPropertySetService customPropertySetService,
                                       LicenseService licenseService,
                                       Clock clock,
                                       MetrologyConfigurationService metrologyConfigurationService) {
        setPropertySpecService(propertySpecService);
        setNlsService(nlsService);
        setMeteringService(meteringService);
        setUserService(userService);
        setThreadPrincipalService(threadPrincipalService);
        setCustomPropertySetService(customPropertySetService);
        setClock(clock);
        setLicenseService(licenseService);
        setMetrologyConfigurationService(metrologyConfigurationService);
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Reference
    public final void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(UsagePointFileImporterMessageHandler.COMPONENT_NAME, Layer.DOMAIN);
    }

    public MeteringService getMeteringService() {
        return meteringService;
    }

    @Reference
    public final void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public final void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    public CustomPropertySetService getCustomPropertySetService() {
        return customPropertySetService;
    }

    @Reference
    public final void setUserService(UserService userService) {
        this.userService = userService;
    }

    public UserService getUserService() {
        return userService;
    }

    @Reference
    public final void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    public ThreadPrincipalService getThreadPrincipalService() {
        return threadPrincipalService;
    }

    public LicenseService getLicenseService() {
        return licenseService;
    }

    @Reference
    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    public Clock getClock() {
        return clock;
    }

    @Reference
    public final void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    public MetrologyConfigurationService getMetrologyConfigurationService() {
        return metrologyConfigurationService;
    }

    public boolean insightInstalled() {
        return getLicenseService().getLicenseForApplication("INS").isPresent();
    }
}
