package com.elster.jupiter.bpm.rest.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.license.License;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component(
        name = "com.elster.jupiter.bpm.rest",
        service = {Application.class},
        immediate = true,
        property = {"alias=/bpm", "app=BPM", "name=" + BpmApplication.COMPONENT_NAME})
public class BpmApplication extends Application {

    public static final String APP_KEY = "BPM";
    public static final String COMPONENT_NAME = "BPM";

    private volatile BpmService bpmService;
    private volatile License license;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(BpmResource.class);
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Reference(target = "(com.elster.jupiter.license.rest.key=" + APP_KEY + ")")
    public void setLicense(License license) {
        this.license = license;
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
            bind(bpmService).to(BpmService.class);
        }
    }
}

