/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmAppService;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.security.Privileges;
import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.users.ApplicationPrivilegesProvider;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component(
        name = "com.elster.jupiter.bpm.app",
        service = {BpmAppService.class, TranslationKeyProvider.class, ApplicationPrivilegesProvider.class},
        immediate = true
)
public class BpmAppServiceImpl implements BpmAppService , TranslationKeyProvider, ApplicationPrivilegesProvider {

    private volatile ServiceRegistration<App> registration;
    private volatile BpmService bpmService;
    private volatile License license;

    // For OSGi purposes
    public BpmAppServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public BpmAppServiceImpl(BpmService bpmService, BundleContext context) {
        this();
        setBpmService(bpmService);
        activate(context);
    }

    @Activate
    public final void activate(BundleContext context) {
        App app = new App(APPLICATION_KEY, APPLICATION_NAME, "connexo", bpmService.getBpmServer().getUrl(), user -> user.getPrivileges(bpmService.COMPONENTNAME).stream().anyMatch(p -> "privilege.design.bpm".equals(p.getName())));
        registration = context.registerService(App.class, app, null);
    }

    @Deactivate
    public void stop(BundleContext context) throws Exception {
        registration.unregister();
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Reference(target = "(com.elster.jupiter.license.application.key=" + APPLICATION_KEY + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    @Override
    public List<String> getApplicationPrivileges() {
        return Arrays.asList(
                Privileges.Constants.DESIGN_BPM
        );
    }

    @Override
    public String getApplicationName() {
        return APPLICATION_KEY;
    }

    @Override
    public String getComponentName() {
        return APPLICATION_KEY;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> translationKeys = new ArrayList<>();
        translationKeys.add(new SimpleTranslationKey(APPLICATION_KEY, APPLICATION_NAME));
        return translationKeys;
    }
}
