/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.app.impl;

import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.users.ApplicationPrivilegesProvider;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.app.MdcAppService;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Component(
        name = "com.energyict.mdc.app",
        service = {MdcAppService.class, TranslationKeyProvider.class, ApplicationPrivilegesProvider.class},
        immediate = true)
@SuppressWarnings("unused")
public class MdcAppServiceImpl implements MdcAppService , TranslationKeyProvider, ApplicationPrivilegesProvider{

    private final Logger logger = Logger.getLogger(MdcAppServiceImpl.class.getName());

    public static final String HTTP_RESOURCE_ALIAS = "/multisense";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/mdc";

    public static final String APPLICATION_ICON = "connexo";

    private volatile ServiceRegistration<App> registration;
    private volatile License license;
    private volatile UserService userService;

    public MdcAppServiceImpl() {
    }

    @Inject
    public MdcAppServiceImpl(UserService userService, BundleContext context) {
        setUserService(userService);
        activate(context);
    }

    @Activate
    public final void activate(BundleContext context) {
        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context), new DefaultStartPage(APPLICATION_NAME));
        App app = new App(APPLICATION_KEY, APPLICATION_NAME, APPLICATION_ICON, HTTP_RESOURCE_ALIAS, resource, this::isAllowed);

        registration = context.registerService(App.class, app, null);
    }

    @Deactivate
    public void stop(BundleContext context) throws Exception {
        registration.unregister();
    }

    @Reference(target = "(com.elster.jupiter.license.application.key=" + APPLICATION_KEY + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private boolean isAllowed(User user) {
        List<? super Privilege> appPrivileges = getDBApplicationPrivileges();
        return user.getPrivileges(APPLICATION_KEY).stream().anyMatch(appPrivileges::contains);
    }

    private List<? super Privilege> getDBApplicationPrivileges() {
        return userService.getPrivileges(APPLICATION_KEY);
    }

    @Override
    public List<String> getApplicationPrivileges() {
        return MdcAppPrivileges.getApplicationPrivileges();
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