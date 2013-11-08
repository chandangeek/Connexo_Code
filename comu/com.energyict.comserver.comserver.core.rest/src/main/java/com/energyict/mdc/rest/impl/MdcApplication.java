package com.energyict.mdc.rest.impl;

import com.energyict.cpo.Environment;
import com.energyict.mdc.services.DeviceProtocolFactoryService;
import com.energyict.mdc.services.ComServerService;
import com.energyict.mdw.core.MeteringWarehouse;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.mdc.rest", service = Application.class, immediate = true, property = {"alias=/mdc"})
public class MdcApplication extends Application {

    private volatile DeviceProtocolFactoryService deviceProtocolFactoryService;
    private volatile ComServerService comServerService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(ComServerResource.class, ComPortResource.class, DeviceCommunicationProtocolsResource.class);
    }

    @Activate
    public void activate(BundleContext context) {
        MeteringWarehouse.createBatchContext(true);
    }

    @Deactivate
    public void deactivate(){
        Environment.getDefault().closeConnection();
    }

    @Reference
    public void setDeviceProtocolFactoryService(DeviceProtocolFactoryService deviceProtocolFactoryService) {
        this.deviceProtocolFactoryService = deviceProtocolFactoryService;
    }

    public DeviceProtocolFactoryService getDeviceProtocolFactoryService() {
        return this.deviceProtocolFactoryService;
    }

    public ComServerService getComServerService() {
        return comServerService;
    }

    @Reference
    public void setComServerService(ComServerService comServerService) {
        this.comServerService = comServerService;
    }

}
