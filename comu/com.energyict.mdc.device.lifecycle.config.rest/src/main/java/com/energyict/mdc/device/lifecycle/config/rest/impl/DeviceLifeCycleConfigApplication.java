package com.energyict.mdc.device.lifecycle.config.rest.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.rest.util.RestValidationExceptionMapper;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.rest.impl.i18n.DefaultLifeCycleTranslationKey;
import com.energyict.mdc.device.lifecycle.config.rest.impl.i18n.MessageSeeds;
import com.energyict.mdc.device.lifecycle.config.rest.i18n.MicroActionTranslationKey;
import com.energyict.mdc.device.lifecycle.config.rest.i18n.MicroCategoryTranslationKey;
import com.energyict.mdc.device.lifecycle.config.rest.i18n.MicroCheckTranslationKey;
import com.energyict.mdc.device.lifecycle.config.rest.impl.resource.DeviceLifeCycleActionResource;
import com.energyict.mdc.device.lifecycle.config.rest.impl.resource.DeviceLifeCycleResource;
import com.energyict.mdc.device.lifecycle.config.rest.impl.resource.DeviceLifeCycleStateResource;
import com.energyict.mdc.device.lifecycle.config.rest.impl.resource.ResourceHelper;
import com.energyict.mdc.device.lifecycle.config.rest.info.AuthorizedActionInfoFactory;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleFactory;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCyclePrivilegeFactory;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleStateFactory;
import com.energyict.mdc.device.lifecycle.config.rest.info.MicroActionAndCheckInfoFactory;
import com.energyict.mdc.device.lifecycle.config.rest.info.StateTransitionEventTypeFactory;
import com.google.common.collect.ImmutableSet;

import java.util.*;
import javax.validation.MessageInterpolator;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
        name = "com.energyict.mdc.device.lifecycle.config.rest",
        service = {Application.class, TranslationKeyProvider.class},
        property = {"alias=/dld", "app=MDC", "name=" + DeviceLifeCycleConfigApplication.DEVICE_CONFIG_LIFECYCLE_COMPONENT},
        immediate = true)
public class DeviceLifeCycleConfigApplication extends Application implements TranslationKeyProvider {
    public static final String DEVICE_CONFIG_LIFECYCLE_COMPONENT = "DLR";

    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile UserService userService;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;

    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private volatile FiniteStateMachineService finiteStateMachineService;
    private volatile EventService eventService;
    private volatile DeviceConfigurationService deviceConfigurationService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(
                TransactionWrapper.class,
                DeviceLifeCycleResource.class,
                DeviceLifeCycleStateResource.class,
                DeviceLifeCycleActionResource.class,
                RestValidationExceptionMapper.class);
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
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
        this.thesaurus = nlsService.getThesaurus(DEVICE_CONFIG_LIFECYCLE_COMPONENT, Layer.REST);
    }


    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Reference
    public void setFiniteStateMachineService(FiniteStateMachineService finiteStateMachineService) {
        this.finiteStateMachineService = finiteStateMachineService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public String getComponentName() {
        return DEVICE_CONFIG_LIFECYCLE_COMPONENT;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>(Arrays.<TranslationKey>asList(MessageSeeds.values()));
        keys.addAll(Arrays.<TranslationKey>asList(DefaultLifeCycleTranslationKey.values()));
        keys.addAll(Arrays.<TranslationKey>asList(MicroActionTranslationKey.values()));
        keys.addAll(Arrays.<TranslationKey>asList(MicroCheckTranslationKey.values()));
        keys.addAll(Arrays.<TranslationKey>asList(MicroCategoryTranslationKey.values()));
        return keys;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(userService).to(UserService.class);
            bind(transactionService).to(TransactionService.class);
            bind(restQueryService).to(RestQueryService.class);
            bind(nlsService).to(NlsService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(thesaurus).to(MessageInterpolator.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(ResourceHelper.class).to(ResourceHelper.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(AuthorizedActionInfoFactory.class).to(AuthorizedActionInfoFactory.class);
            bind(DeviceLifeCycleStateFactory.class).to(DeviceLifeCycleStateFactory.class);
            bind(DeviceLifeCyclePrivilegeFactory.class).to(DeviceLifeCyclePrivilegeFactory.class);
            bind(StateTransitionEventTypeFactory.class).to(StateTransitionEventTypeFactory.class);
            bind(DeviceLifeCycleFactory.class).to(DeviceLifeCycleFactory.class);
            bind(MicroActionAndCheckInfoFactory.class).to(MicroActionAndCheckInfoFactory.class);

            bind(deviceLifeCycleConfigurationService).to(DeviceLifeCycleConfigurationService.class);
            bind(finiteStateMachineService).to(FiniteStateMachineService.class);
            bind(eventService).to(EventService.class);
            bind(deviceConfigurationService).to(DeviceConfigurationService.class);
        }
    }
}
