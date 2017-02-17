/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl;

import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.metering.slp.SyntheticLoadProfileService;
import com.elster.jupiter.users.UserService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;

@Component(name = "ccom.elster.jupiter.slp.imports.impl.SyntheticLoadProfileDataImporterContext", service = {SyntheticLoadProfileDataImporterContext.class})
public class SyntheticLoadProfileDataImporterContext {
    private volatile SyntheticLoadProfileService syntheticLoadProfileService;
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private volatile UserService userService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile LicenseService licenseService;
    private volatile Clock clock;

    public SyntheticLoadProfileDataImporterContext() {
    }

    @Inject
    public SyntheticLoadProfileDataImporterContext(SyntheticLoadProfileService syntheticLoadProfileService,
                                                   PropertySpecService propertySpecService,
                                                   NlsService nlsService,
                                                   UserService userService,
                                                   ThreadPrincipalService threadPrincipalService,
                                                   LicenseService licenseService,
                                                   Clock clock) {
        setSyntheticLoadProfileService(syntheticLoadProfileService);
        setPropertySpecService(propertySpecService);
        setNlsService(nlsService);
        setUserService(userService);
        setThreadPrincipalService(threadPrincipalService);
        setClock(clock);
        setLicenseService(licenseService);
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
        this.thesaurus = nlsService.getThesaurus(SyntheticLoadProfileFileImporterMessageHandler.COMPONENT_NAME, Layer.DOMAIN);
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

    public SyntheticLoadProfileService getSyntheticLoadProfileService() {
        return syntheticLoadProfileService;
    }

    @Reference
    public void setSyntheticLoadProfileService(SyntheticLoadProfileService syntheticLoadProfileService) {
        this.syntheticLoadProfileService = syntheticLoadProfileService;
    }

    public boolean insightInstalled() {
        return getLicenseService().getLicenseForApplication("INS").isPresent();
    }


}
