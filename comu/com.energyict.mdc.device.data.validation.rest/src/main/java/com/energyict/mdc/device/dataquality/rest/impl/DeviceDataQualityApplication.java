/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.rest.impl;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.dataquality.DeviceDataQualityService;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.energyict.ddq.rest", service = {Application.class}, immediate = true, property = {"alias=/ddq", "app=MDC", "name=" + DeviceDataQualityApplication.COMPONENT_NAME})
public class DeviceDataQualityApplication extends Application implements MessageSeedProvider {

    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "DQR";

    private volatile DeviceDataQualityService deviceDataQualityService;
    private volatile DataQualityKpiService dataQualityKpiService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile ValidationService validationService;
    private volatile EstimationService estimationService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile Thesaurus thesaurus;
    private volatile License license;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                DataQualityResultsResource.class,
                FieldResource.class
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
    public void setDeviceDataQualityService(DeviceDataQualityService deviceDataQualityService) {
        this.deviceDataQualityService = deviceDataQualityService;
    }

    @Reference
    public void setDataQualityKpiService(DataQualityKpiService dataQualityKpiService) {
        this.dataQualityKpiService = dataQualityKpiService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @org.osgi.service.component.annotations.Reference(target = "(com.elster.jupiter.license.rest.key=" + APP_KEY + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(deviceDataQualityService).to(DeviceDataQualityService.class);
            bind(dataQualityKpiService).to(DataQualityKpiService.class);
            bind(meteringGroupsService).to(MeteringGroupsService.class);
            bind(validationService).to(ValidationService.class);
            bind(estimationService).to(EstimationService.class);
            bind(deviceConfigurationService).to(DeviceConfigurationService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(DataQualityOverviewInfoFactory.class).to(DataQualityOverviewInfoFactory.class);
            bind(ResourceHelper.class).to(ResourceHelper.class);
        }
    }
}


