package com.energyict.mdc.rest.impl;

import com.energyict.cpo.Environment;
import com.energyict.mdc.services.ComServerService;
import com.energyict.mdc.services.DeviceProtocolPluggableClassService;
import com.energyict.mdc.services.DeviceProtocolService;
import com.energyict.mdc.services.InboundDeviceProtocolPluggableClassService;
import com.energyict.mdc.services.InboundDeviceProtocolService;
import com.energyict.mdw.core.MeteringWarehouse;
import com.google.common.collect.ImmutableSet;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Set;

@Component(name = "com.elster.mdc.rest", service = Application.class, immediate = true, property = {"alias=/mdc"})
public class MdcApplication extends Application {

    private volatile DeviceProtocolPluggableClassService deviceProtocolPluggableClassService;
    private volatile DeviceProtocolService deviceProtocolService;
    private volatile ComServerService comServerService;
    private volatile InboundDeviceProtocolService inboundDeviceProtocolService;
    private volatile InboundDeviceProtocolPluggableClassService inboundDeviceProtocolPluggableClassService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(ComServerResource.class,
                ComPortResource.class,
                DeviceCommunicationProtocolsResource.class,
                FieldResource.class,
                DeviceDiscoveryProtocolsResource.class);
    }

    @Activate
    public void activate(BundleContext context) {
        System.out.println("Starting the MDC application in the MDC Rest bundle");
        MeteringWarehouse.createBatchContext(true);
    }

    @Deactivate
    public void deactivate(){
        Environment.getDefault().closeConnection();
    }

    public ComServerService getComServerService() {
        return comServerService;
    }

    @Reference
    public void setComServerService(ComServerService comServerService) {
        this.comServerService = comServerService;
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
}
