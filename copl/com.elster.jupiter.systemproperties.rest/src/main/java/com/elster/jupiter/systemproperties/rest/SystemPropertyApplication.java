package com.elster.jupiter.systemproperties.rest;


import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.systemproperties.SystemPropertyService;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component(name = "com.elster.jupiter.systemproperties.rest",
        service = {Application.class}, immediate = true,
        property = {"alias=/sp", "app=" + SystemPropertyApplication.APP_KEY, "name=" + SystemPropertyApplication.COMPONENT_NAME})
public class SystemPropertyApplication extends Application {

    public static final String APP_KEY = "SYS";
    public static final String COMPONENT_NAME = "SYR";
    private volatile SystemPropertyService systemPropertiesService;
    private volatile PropertyValueInfoService propertyValueInfoService;


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

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService){
        this.propertyValueInfoService = propertyValueInfoService;
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
            bind(propertyValueInfoService).to(PropertyValueInfoService.class);
        }
    }


}
