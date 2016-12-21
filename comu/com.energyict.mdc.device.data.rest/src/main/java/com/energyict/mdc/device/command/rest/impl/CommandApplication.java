package com.energyict.mdc.device.command.rest.impl;

import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;

import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import com.google.common.collect.ImmutableSet;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.energyict.mdc.device.command.rest",
        service = {Application.class, TranslationKeyProvider.class, MessageSeedProvider.class}, immediate = true, property = {"alias=/crr", "app=MDC", "name=" + CommandApplication.COMPONENT_NAME})
public class CommandApplication extends javax.ws.rs.core.Application implements TranslationKeyProvider, MessageSeedProvider{

    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "CRR";
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile JsonService jsonService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile CommandRuleService commandRuleService;

    private volatile License license;
    private volatile ExceptionFactory exceptionFactory;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                CommandRuleResource.class
        );
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference
    public void setCommandRuleService(CommandRuleService commandRuleService) {
        this.commandRuleService = commandRuleService;
    }

    @Reference(target = "(com.elster.jupiter.license.rest.key=" + APP_KEY + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    @Reference
    public void setExceptionFactory(ExceptionFactory exceptionFactory) {
        this.exceptionFactory = exceptionFactory;
    }

    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Collections.emptyList();
    }


    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }



    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(nlsService).to(NlsService.class);
            bind(jsonService).to(JsonService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(deviceMessageSpecificationService).to(DeviceMessageSpecificationService.class);
            bind(commandRuleService).to(CommandRuleService.class);
            bind(CommandRuleInfoFactory.class).to(CommandRuleInfoFactory.class);
            bind(exceptionFactory).to(ExceptionFactory.class);
        }
    }
}
