package com.elster.jupiter.systemproperties;


import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component(name = "com.elster.jupiter.systemproperties",
        service = {Application.class}, immediate = true,
        property = {"alias=/sp", "app=" + SystemPropertyApllication.APP_KEY, "name=" + SystemPropertyApllication.COMPONENT_NAME})
public class SystemPropertyApllication extends Application{

    public static final String APP_KEY = "SYS";
    public static final String COMPONENT_NAME = "SP";
    private volatile SystemPropertyService systemPropertiesService;


    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
            SystemPropertyResource.class
        );
    }

    @Reference
    public void setSystemPropertiesService(SystemPropertyService systemPropertiesService){
        this.systemPropertiesService = systemPropertiesService;
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
            bind(systemPropertiesService).to(SystemPropertyService.class);
        }
    }


}
