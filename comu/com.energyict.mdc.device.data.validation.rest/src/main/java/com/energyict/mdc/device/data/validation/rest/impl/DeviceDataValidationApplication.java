package com.energyict.mdc.device.data.validation.rest.impl;

import com.elster.jupiter.license.License;
import com.energyict.mdc.device.data.validation.DeviceDataValidationService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by dragos on 7/21/2015.
 *
 */

@Component(name = "com.energyict.dvr.rest", service = {Application.class}, immediate = true, property = {"alias=/dvr", "app=MDC", "name=" + DeviceDataValidationApplication.COMPONENT_NAME})
public class DeviceDataValidationApplication extends Application {

    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "DVR";

    private volatile DeviceDataValidationService deviceDataValidationService;
    private volatile License license;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                ValidationResultsResource.class
        );
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Reference
    public void setDeviceDataValidationService(DeviceDataValidationService deviceDataValidationService) {
        this.deviceDataValidationService = deviceDataValidationService;
    }

    @org.osgi.service.component.annotations.Reference(target = "(com.elster.jupiter.license.rest.key=" + APP_KEY + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(deviceDataValidationService).to(DeviceDataValidationService.class);
        }
    }
}


