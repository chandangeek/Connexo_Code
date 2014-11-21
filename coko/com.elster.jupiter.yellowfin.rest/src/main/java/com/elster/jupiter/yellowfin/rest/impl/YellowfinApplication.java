package com.elster.jupiter.yellowfin.rest.impl;

import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.yellowfin.YellowfinService;
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

@Component(name = "com.elster.jupiter.yellowfin.rest" , service=Application.class , immediate = true , property = {"alias=/yfn"} )
public class YellowfinApplication extends Application implements BinderProvider{

    private final Set<Class<?>> classes = new HashSet<>();

    private volatile YellowfinService yellowfinService;

    public YellowfinApplication() {
        classes.add(YellowfinResource.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    @Reference
    public void setYellowfinService(YellowfinService yellowfinService) {
        this.yellowfinService = yellowfinService;
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
                bind(yellowfinService).to(YellowfinService.class);
            }
        };
    }
}

