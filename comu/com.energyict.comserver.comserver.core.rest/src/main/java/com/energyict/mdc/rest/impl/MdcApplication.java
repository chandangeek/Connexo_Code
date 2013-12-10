package com.energyict.mdc.rest.impl;

import com.energyict.mdc.services.ComPortPoolService;
import com.energyict.mdc.services.ComPortService;
import com.energyict.mdc.services.ComServerService;
import com.energyict.mdc.services.DeviceProtocolPluggableClassService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.services.InboundDeviceProtocolPluggableClassService;
import com.energyict.mdc.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.mdc.rest", service = Application.class, immediate = true, property = {"alias=/mdc"})
public class MdcApplication extends Application {

    private static final Logger LOGGER = Logger.getLogger(MdcApplication.class.getSimpleName());

    private volatile DeviceProtocolPluggableClassService deviceProtocolPluggableClassService;
    private volatile DeviceProtocolService deviceProtocolService;
    private volatile ComPortService comPortService;
    private volatile InboundDeviceProtocolService inboundDeviceProtocolService;
    private volatile InboundDeviceProtocolPluggableClassService inboundDeviceProtocolPluggableClassService;
    private volatile LicensedProtocolService licensedProtocolService;
    private volatile ComServerService comServerService;
    private volatile ComPortPoolService comPortPoolService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(AutoCloseDatabaseConnection.class,
                ComServerResource.class,
                ComPortResource.class,
                ComPortPoolResource.class,
                DeviceCommunicationProtocolsResource.class,
                FieldResource.class,
                DeviceDiscoveryProtocolsResource.class,
                LicensedProtocolResource.class,
                TimeZoneInUseResource.class,
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
    public void setDeviceProtocolPluggableClassService(DeviceProtocolPluggableClassService deviceProtocolPluggableClassService) {
        this.deviceProtocolPluggableClassService = deviceProtocolPluggableClassService;
    }

    @Reference
    public void setDeviceProtocolService(DeviceProtocolService deviceProtocolService) {
        this.deviceProtocolService = deviceProtocolService;
    }

    @Reference
    public void setComPortPoolService(ComPortPoolService comPortPoolService) {
        this.comPortPoolService = comPortPoolService;
    }

    @Reference
    public void setComServerService(ComServerService comServerService) {
        this.comServerService = comServerService;
    }

    @Reference
    public void setComPortService(ComPortService comPortService) {
        this.comPortService = comPortService;
    }

    @Reference
    public void setInboundDeviceProtocolService(InboundDeviceProtocolService inboundDeviceProtocolService) {
        this.inboundDeviceProtocolService = inboundDeviceProtocolService;
    }

    @Reference
    public void setInboundDeviceProtocolPluggableClassService(InboundDeviceProtocolPluggableClassService inboundDeviceProtocolPluggableClassService) {
        this.inboundDeviceProtocolPluggableClassService = inboundDeviceProtocolPluggableClassService;
    }

    @Reference
    public void setLicensedProtocolService(LicensedProtocolService licensedProtocolService) {
        this.licensedProtocolService = licensedProtocolService;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            LOGGER.fine("Binding services using HK2");
            bind(comServerService).to(ComServerService.class);
            bind(comPortService).to(ComPortService.class);
            bind(deviceProtocolPluggableClassService).to(DeviceProtocolPluggableClassService.class);
            bind(deviceProtocolService).to(DeviceProtocolService.class);
            bind(inboundDeviceProtocolService).to(InboundDeviceProtocolService.class);
            bind(inboundDeviceProtocolPluggableClassService).to(InboundDeviceProtocolPluggableClassService.class);
            bind(licensedProtocolService).to(LicensedProtocolService.class);
            bind(comPortPoolService).to(ComPortPoolService.class);
        }
    }
}
