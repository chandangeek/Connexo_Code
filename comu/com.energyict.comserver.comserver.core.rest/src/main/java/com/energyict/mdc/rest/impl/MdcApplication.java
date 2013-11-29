package com.energyict.mdc.rest.impl;

import com.energyict.mdc.services.ComPortPoolService;
import com.energyict.mdc.services.ComPortService;
import com.energyict.mdc.services.ComServerService;
import com.energyict.mdc.services.DeviceProtocolPluggableClassService;
import com.energyict.mdc.services.DeviceProtocolService;
import com.energyict.mdc.services.InboundDeviceProtocolPluggableClassService;
import com.energyict.mdc.services.InboundDeviceProtocolService;
import com.energyict.mdc.services.LicensedProtocolService;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.mdc.rest", service = Application.class, immediate = true, property = {"alias=/mdc"})
public class MdcApplication extends Application {

    private volatile DeviceProtocolPluggableClassService deviceProtocolPluggableClassService;
    private volatile DeviceProtocolService deviceProtocolService;
    private volatile ComPortService comPortService;
    private volatile InboundDeviceProtocolService inboundDeviceProtocolService;
    private volatile InboundDeviceProtocolPluggableClassService inboundDeviceProtocolPluggableClassService;
    private volatile LicensedProtocolService licensedProtocolService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(CloseDatabaseEventListener.class,
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

    @Reference
    public void setDeviceProtocolPluggableClassService(DeviceProtocolPluggableClassService deviceProtocolPluggableClassService) {
        this.deviceProtocolPluggableClassService = deviceProtocolPluggableClassService;
    }

    public DeviceProtocolPluggableClassService getDeviceProtocolPluggableClassService() {
        return deviceProtocolPluggableClassService;
    }

    @Reference
    public void setDeviceProtocolService(DeviceProtocolService deviceProtocolService) {
        this.deviceProtocolService = deviceProtocolService;
    }

    @Reference
    public void setComPortPoolService(ComPortPoolService comPortPoolService) {
        ComPortPoolServiceHolder.setComPortPoolService(comPortPoolService);
    }

    @Reference
    public void setComServerService(ComServerService comServerService) {
        ComServerServiceHolder.setComServerService(comServerService);
    }
    
    public ComPortService getComPortService() {
        return comPortService;
    }

    @Reference
    public void setComPortService(ComPortService comPortService) {
        this.comPortService = comPortService;
    }
    public DeviceProtocolService getDeviceProtocolService() {
        return deviceProtocolService;
    }

    @Reference
    public void setInboundDeviceProtocolService(InboundDeviceProtocolService inboundDeviceProtocolService) {
        this.inboundDeviceProtocolService = inboundDeviceProtocolService;
    }

    public InboundDeviceProtocolService getInboundDeviceProtocolService() {
        return inboundDeviceProtocolService;
    }

    @Reference
    public void setInboundDeviceProtocolPluggableClassService(InboundDeviceProtocolPluggableClassService inboundDeviceProtocolPluggableClassService) {
        this.inboundDeviceProtocolPluggableClassService = inboundDeviceProtocolPluggableClassService;
    }

    public InboundDeviceProtocolPluggableClassService getInboundDeviceProtocolPluggableClassService() {
        return inboundDeviceProtocolPluggableClassService;
    }

    @Reference
    public void setLicensedProtocolService(LicensedProtocolService licensedProtocolService) {
        this.licensedProtocolService = licensedProtocolService;
    }

    public LicensedProtocolService getLicensedProtocolService() {
        return licensedProtocolService;
    }
}
