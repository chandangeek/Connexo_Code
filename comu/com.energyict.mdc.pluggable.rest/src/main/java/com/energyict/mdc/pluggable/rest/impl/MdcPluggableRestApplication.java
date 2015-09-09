package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.UserFileFactory;
import com.energyict.mdc.protocol.api.codetables.CodeFactory;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
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

@Component(name = "com.energyict.mdc.pluggable.rest", service = {Application.class, MessageSeedProvider.class}, immediate = true, property = {"alias=/plr", "app=MDC", "name=" + MdcPluggableRestApplication.COMPONENT_NAME})
public class MdcPluggableRestApplication extends Application implements MessageSeedProvider {
    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "PLR";

    private volatile CodeFactory codeFactory;
    private volatile UserFileFactory userFileFactory;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile PropertySpecService propertySpecService;
    private volatile TransactionService transactionService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile License license;
    private volatile FirmwareService firmwareService;
    private NlsService nlsService;
    private Thesaurus thesaurus;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                TransactionWrapper.class,
                DeviceCommunicationProtocolsResource.class,
                DeviceDiscoveryProtocolsResource.class,
                LicensedProtocolResource.class,
                TimeZoneInUseResource.class,
                UserFileReferenceResource.class,
                LoadProfileTypeResource.class,
                CodeTableResource.class);
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Reference
    public void setCodeFactory(CodeFactory codeFactory) {
        this.codeFactory = codeFactory;
    }

    @Reference
    public void setUserFileFactory(UserFileFactory userFileFactory) {
        this.userFileFactory = userFileFactory;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference(target="(com.elster.jupiter.license.rest.key=" + APP_KEY  + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    @Reference
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(codeFactory).to(CodeFactory.class);
            bind(userFileFactory).to(UserFileFactory.class);
            bind(protocolPluggableService).to(ProtocolPluggableService.class);
            bind(propertySpecService).to(PropertySpecService.class);
            bind(transactionService).to(TransactionService.class);
            bind(nlsService).to(NlsService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(deviceConfigurationService).to(DeviceConfigurationService.class);
            bind(MdcPropertyUtils.class).to(MdcPropertyUtils.class);
            bind(firmwareService).to(FirmwareService.class);
        }
    }

}