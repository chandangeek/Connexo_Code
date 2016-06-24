package com.energyict.mdc.test.api.rest.impl;

import com.elster.jupiter.metering.MeteringService;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component(name = "com.energyict.mdc.test.api.rest",
        service = {Application.class},
        immediate = true,
        property = {"alias=/test1", "app=MDC", "name=" + TestUsagePointResourceApplication.COMPONENT_NAME})
public class TestUsagePointResourceApplication extends Application {

    public static final String COMPONENT_NAME = "MDC_TEST";

    private volatile MeteringService meteringService;

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Activate
    public void activate() {
        System.out.println("Activated.");
    }

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(UsagePointTestResource.class);
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
        public void configure() {
            bind(meteringService).to(MeteringService.class);
        }
    }
}
