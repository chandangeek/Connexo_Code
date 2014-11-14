package com.energyict.mdc.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.JsonMappingExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.rest.Installer;
import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.rest.impl.comserver.ComPortPoolComPortResource;
import com.energyict.mdc.rest.impl.comserver.ComPortPoolResource;
import com.energyict.mdc.rest.impl.comserver.ComPortResource;
import com.energyict.mdc.rest.impl.comserver.ComServerComPortResource;
import com.energyict.mdc.rest.impl.comserver.ComServerResource;
import com.energyict.mdc.rest.impl.comserver.MessageSeeds;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.energyict.mdc.rest", service = {Application.class, InstallService.class}, immediate = true, property = {"alias=/mdc", "app=MDC", "name=" + MdcApplication.COMPONENT_NAME})
public class MdcApplication extends Application implements InstallService {
    public static final String COMPONENT_NAME = "CCR";

    private volatile EngineModelService engineModelService;
    private volatile TransactionService transactionService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                TransactionWrapper.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedExceptionMapper.class,
                JsonMappingExceptionMapper.class,
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
    public void setEngineModelService(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Override
    public void install() {
        new Installer().createTranslations(COMPONENT_NAME, thesaurus, Layer.REST, MessageSeeds.values());
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("NLS");
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(engineModelService).to(EngineModelService.class);
            bind(transactionService).to(TransactionService.class);
            bind(deviceConfigurationService).to(DeviceConfigurationService.class);
            bind(protocolPluggableService).to(ProtocolPluggableService.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(nlsService).to(NlsService.class);
            bind(thesaurus).to(Thesaurus.class);
        }
    }

}