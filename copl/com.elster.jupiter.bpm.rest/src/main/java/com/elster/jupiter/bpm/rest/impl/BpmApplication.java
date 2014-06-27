package com.elster.jupiter.bpm.rest.impl;

import com.elster.jupiter.bpm.BpmService;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.ComponentContext;
import com.elster.jupiter.rest.util.BinderProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@Component(name = "com.elster.jupiter.bpm.rest" , service=Application.class , immediate = true , property = {"alias=/bpm"} )
public class BpmApplication extends Application implements BinderProvider{

    private final Set<Class<?>> classes = new HashSet<>();

    private volatile BpmService bpmService;

    public BpmApplication() {
        classes.add(BpmResource.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    @Reference
    public void setDeviceDataService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Activate
    public void activate(ComponentContext context) {
        BpmStartup.init(context.getBundleContext());
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
    }

    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(bpmService).to(BpmService.class);
            }
        };
    }
}

