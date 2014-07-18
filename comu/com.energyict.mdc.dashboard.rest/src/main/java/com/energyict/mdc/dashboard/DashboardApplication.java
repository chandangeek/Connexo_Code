package com.energyict.mdc.dashboard;

import com.energyict.mdc.engine.status.StatusService;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (10:32)
 */
@Component(name = "com.energyict.dsb.rest", service = Application.class, immediate = true, property = {"alias=/dsb"})
public class DashboardApplication extends Application {

    public static final String COMPONENT_NAME = "DSB";

    private volatile StatusService statusService;

    @Reference
    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
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
            bind(statusService).to(StatusService.class);
        }
    }

}