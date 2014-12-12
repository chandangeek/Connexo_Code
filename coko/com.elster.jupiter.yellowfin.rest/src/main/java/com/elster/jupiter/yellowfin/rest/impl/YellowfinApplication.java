package com.elster.jupiter.yellowfin.rest.impl;

import com.elster.jupiter.license.License;
import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.yellowfin.YellowfinService;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@Component(name = "com.elster.jupiter.yellowfin.rest" , service=Application.class , immediate = true , property = {"alias=/yfn", "app=YFN", "name=" + YellowfinApplication.COMPONENT_NAME} )
public class YellowfinApplication extends Application implements BinderProvider{
    public static final String APP_KEY = "YFN";
    public static final String COMPONENT_NAME = "YFN";

    private final Set<Class<?>> classes = new HashSet<>();

    private volatile YellowfinService yellowfinService;
    private volatile License license;

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

    @Reference(target="(com.elster.jupiter.license.rest.key=" + APP_KEY  + ")")
    public void setLicense(License license) {
        this.license = license;
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

