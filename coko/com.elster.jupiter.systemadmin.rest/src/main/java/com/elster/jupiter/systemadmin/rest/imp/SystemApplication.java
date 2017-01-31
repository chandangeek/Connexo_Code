/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp;

import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.system.SubsystemService;
import com.elster.jupiter.systemadmin.rest.imp.resource.BundleTypeTranslationKeys;
import com.elster.jupiter.systemadmin.rest.imp.resource.ComponentResource;
import com.elster.jupiter.systemadmin.rest.imp.resource.ComponentStatusTranslationKeys;
import com.elster.jupiter.systemadmin.rest.imp.resource.DataPurgeResource;
import com.elster.jupiter.systemadmin.rest.imp.resource.FieldResource;
import com.elster.jupiter.systemadmin.rest.imp.resource.LicenseResource;
import com.elster.jupiter.systemadmin.rest.imp.resource.LicenseStatusTranslationKeys;
import com.elster.jupiter.systemadmin.rest.imp.resource.LicenseTypeTranslationKeys;
import com.elster.jupiter.systemadmin.rest.imp.resource.MessageSeeds;
import com.elster.jupiter.systemadmin.rest.imp.resource.SystemInfoResource;
import com.elster.jupiter.systemadmin.rest.imp.response.ApplicationInfoFactory;
import com.elster.jupiter.systemadmin.rest.imp.response.BundleTypeInfoFactory;
import com.elster.jupiter.systemadmin.rest.imp.response.ComponentInfoFactory;
import com.elster.jupiter.systemadmin.rest.imp.response.ComponentStatusInfoFactory;
import com.elster.jupiter.systemadmin.rest.imp.response.LicenseInfoFactory;
import com.elster.jupiter.systemadmin.rest.imp.response.SystemInfoFactory;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.validation.MessageInterpolator;
import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(
        name = "com.elster.jupiter.systemadmin.rest",
        service = {Application.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        immediate = true,
        property = {"alias=/sys", "app=SYS", "name=" + SystemApplication.COMPONENT_NAME})
public class SystemApplication extends Application implements MessageSeedProvider, TranslationKeyProvider {
    public static final String COMPONENT_NAME = "SYS";

    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile UserService userService;
    private volatile LicenseService licenseService;
    private volatile NlsService nlsService;
    private volatile LifeCycleService lifeCycleService;
    private volatile TaskService taskService;
    private volatile Thesaurus thesaurus;
    private volatile JsonService jsonService;
    private volatile SubsystemService subsystemService;
    private volatile Clock clock;

    private BundleContext bundleContext;
    private Long lastStartedTime;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                LicenseResource.class,
                DataPurgeResource.class,
                MultiPartFeature.class,
                SystemInfoResource.class,
                FieldResource.class,
                ComponentResource.class);
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        this.lastStartedTime = System.currentTimeMillis();
        this.bundleContext = bundleContext;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, getLayer());
    }

    @Reference
    public void setLifeCycleService(LifeCycleService lifeCycleService) {
        this.lifeCycleService = lifeCycleService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setSubsystemService(SubsystemService subsystemService) {
        this.subsystemService = subsystemService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        keys.addAll(Arrays.asList(BundleTypeTranslationKeys.values()));
        keys.addAll(Arrays.asList(ComponentStatusTranslationKeys.values()));
        keys.addAll(Arrays.asList(LicenseTypeTranslationKeys.values()));
        keys.addAll(Arrays.asList(LicenseStatusTranslationKeys.values()));
        return keys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(licenseService).to(LicenseService.class);
            bind(userService).to(UserService.class);
            bind(transactionService).to(TransactionService.class);
            bind(restQueryService).to(RestQueryService.class);
            bind(nlsService).to(NlsService.class);
            bind(lifeCycleService).to(LifeCycleService.class);
            bind(taskService).to(TaskService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(thesaurus).to(MessageInterpolator.class);
            bind(jsonService).to(JsonService.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(SystemInfoFactory.class).to(SystemInfoFactory.class);
            bind(ApplicationInfoFactory.class).to(ApplicationInfoFactory.class);
            bind(ComponentInfoFactory.class).to(ComponentInfoFactory.class);
            bind(BundleTypeInfoFactory.class).to(BundleTypeInfoFactory.class);
            bind(ComponentStatusInfoFactory.class).to(ComponentStatusInfoFactory.class);
            bind(subsystemService).to(SubsystemService.class);
            bind(bundleContext).to(BundleContext.class);
            bind(lastStartedTime).to(Long.class).named("LAST_STARTED_TIME");
            bind(LicenseInfoFactory.class).to(LicenseInfoFactory.class);
            bind(NlsService.class).to(NlsService.class);
            bind(clock).to(Clock.class);
        }
    }
}
