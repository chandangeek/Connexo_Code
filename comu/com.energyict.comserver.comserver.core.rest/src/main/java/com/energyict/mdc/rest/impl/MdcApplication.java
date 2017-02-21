/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl;

import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.rest.impl.comserver.ComPortInfoFactory;
import com.energyict.mdc.rest.impl.comserver.ComPortPoolComPortResource;
import com.energyict.mdc.rest.impl.comserver.ComPortPoolInfoFactory;
import com.energyict.mdc.rest.impl.comserver.ComPortPoolResource;
import com.energyict.mdc.rest.impl.comserver.ComPortResource;
import com.energyict.mdc.rest.impl.comserver.ComServerComPortResource;
import com.energyict.mdc.rest.impl.comserver.ComServerFieldTranslationKeys;
import com.energyict.mdc.rest.impl.comserver.ComServerInfoFactory;
import com.energyict.mdc.rest.impl.comserver.ComServerResource;
import com.energyict.mdc.rest.impl.comserver.MessageSeeds;
import com.energyict.mdc.rest.impl.comserver.ResourceHelper;
import com.energyict.mdc.rest.impl.comserver.TimeDurationUnitTranslationKeys;
import com.energyict.mdc.rest.impl.comserver.TranslationKeys;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.energyict.mdc.rest", service = {Application.class, TranslationKeyProvider.class, MessageSeedProvider.class}, immediate = true, property = {"alias=/mdc", "app=MDC", "name=" + MdcApplication.COMPONENT_NAME})
public class MdcApplication extends Application implements TranslationKeyProvider, MessageSeedProvider {
    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "CCR";

    private volatile EngineConfigurationService engineConfigurationService;
    private volatile TransactionService transactionService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile License license;
    private PropertyValueInfoService propertyValueInfoService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                ComServerResource.class,
                ComServerComPortResource.class,
                ComPortResource.class,
                ComPortPoolResource.class,
                ComPortPoolComPortResource.class,
                ComServerFieldResource.class);
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Reference
    public void setEngineConfigurationService(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Override
    public String getComponentName() {
        return MdcApplication.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys =  new ArrayList<>();
        keys.addAll(Arrays.asList(TimeDurationUnitTranslationKeys.values()));
        keys.addAll(Arrays.asList(ComServerFieldTranslationKeys.values()));
        keys.addAll(Arrays.asList(TranslationKeys.values()));
        return keys;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference(target="(com.elster.jupiter.license.rest.key=" + APP_KEY  + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(engineConfigurationService).to(EngineConfigurationService.class);
            bind(transactionService).to(TransactionService.class);
            bind(deviceConfigurationService).to(DeviceConfigurationService.class);
            bind(protocolPluggableService).to(ProtocolPluggableService.class);
            bind(propertyValueInfoService).to(PropertyValueInfoService.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(nlsService).to(NlsService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(MdcPropertyUtils.class).to(MdcPropertyUtils.class);
            bind(ResourceHelper.class).to(ResourceHelper.class);
            bind(ComPortInfoFactory.class).to(ComPortInfoFactory.class);
            bind(ComPortPoolInfoFactory.class).to(ComPortPoolInfoFactory.class);
            bind(ComServerInfoFactory.class).to(ComServerInfoFactory.class);
        }
    }

}