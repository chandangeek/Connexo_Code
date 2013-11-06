package com.energyict.mdc.rest.impl;

import com.energyict.mdw.core.MeteringWarehouse;
import com.google.common.collect.ImmutableSet;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import javax.ws.rs.core.Application;
import java.util.Set;

@Component(name = "com.elster.mdc.rest", service = Application.class, immediate = true, property = {"alias=/mdc"})
public class MdcApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(ComServerRest.class, DeviceCommunicationProtocolsResource.class);
    }

    @Activate
    public void activate(BundleContext context) {
        MeteringWarehouse.createBatchContext(true);
    }


}
