package com.energyict.mdc.rest;

import com.energyict.mdw.core.MeteringWarehouse;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(name = "com.elster.mdc.rest" , service=Application.class , immediate = true , property = {"alias=/mdc"} )
public class ComServerApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(ComServerRest.class);
    }

    @Activate
    public void activate(BundleContext context) {
        MeteringWarehouse.createBatchContext(true);
    }


}
