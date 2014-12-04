package com.elster.jupiter.bpm.rest.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.rest.util.BinderProvider;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@Component(name = "com.elster.jupiter.bpm.rest" , service=Application.class , immediate = true , property = {"alias=/bpm", "app=BPM", "name=" + BpmApplication.COMPONENT_NAME} )
public class BpmApplication extends Application implements BinderProvider{

    public static final String APP_KEY = "BPM";
    public static final String COMPONENT_NAME = "BPM";

    private final Set<Class<?>> classes = new HashSet<>();

    private volatile BpmService bpmService;
    private volatile License license;

    public BpmApplication() {
        classes.add(BpmResource.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Reference(target="(com.elster.jupiter.license.application.key=" + APP_KEY  + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    @Activate
    public void activate(ComponentContext context) {
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

