package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.rest.AutoCloseDatabaseConnection;
import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.pluggable.rest", service = Application.class, immediate = true, property = {"alias=/plr"})
public class MdcPluggableRestApplication extends Application {

    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile LicensedProtocolService licensedProtocolService;
    private volatile PropertySpecService propertySpecService;
    private volatile TransactionService transactionService;
    private NlsService nlsService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(AutoCloseDatabaseConnection.class,
                TransactionWrapper.class,
                ConstraintViolationExceptionMapper.class,
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
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setLicensedProtocolService(LicensedProtocolService licensedProtocolService) {
        this.licensedProtocolService = licensedProtocolService;
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
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(protocolPluggableService).to(ProtocolPluggableService.class);
            bind(licensedProtocolService).to(LicensedProtocolService.class);
            bind(propertySpecService).to(PropertySpecService.class);
            bind(transactionService).to(TransactionService.class);
            bind(nlsService).to(NlsService.class);
        }
    }

}