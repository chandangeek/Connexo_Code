/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.test.api.rest.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.DeviceService;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component(name = "com.energyict.mdc.test.api.rest",
        service = {Application.class},
        immediate = true,
        property = {"alias=/testmdc", "app=MDC", "name=" + MdcTestApiApplication.COMPONENT_NAME})
public class MdcTestApiApplication extends Application {

    public static final String COMPONENT_NAME = "MDC_TEST";

    private volatile TimeOfUseCampaignService timeOfUseCampaignService;
    private volatile CalendarService calendarService;
    private volatile Thesaurus thesaurus;
    private volatile DeviceService deviceService;
    private volatile FirmwareService firmwareService;
    private volatile OrmService ormService;

    public MdcTestApiApplication() {
        //for OSGI
    }

    @Inject
    public MdcTestApiApplication(TimeOfUseCampaignService timeOfUseCampaignService, CalendarService calendarService,
                                 Thesaurus thesaurus, FirmwareService firmwareService, OrmService ormService,
                                 DeviceService deviceService) {
        this.timeOfUseCampaignService = timeOfUseCampaignService;
        this.calendarService = calendarService;
        this.thesaurus = thesaurus;
        this.firmwareService = firmwareService;
        this.ormService = ormService;
        this.deviceService = deviceService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setTimeOfUseCampaignService(TimeOfUseCampaignService timeOfUseCampaignService) {
        this.timeOfUseCampaignService = timeOfUseCampaignService;
    }

    @Reference
    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(TimeOfUseCampaignTestResource.class,
                CalendarTestResource.class,
                ServiceKeyTestResource.class,
                FirmwareTestResource.class);
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
            bind(timeOfUseCampaignService).to(TimeOfUseCampaignService.class);
            bind(calendarService).to(CalendarService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(deviceService).to(DeviceService.class);
            bind(firmwareService).to(FirmwareService.class);
            bind(ormService).to(OrmService.class);
        }
    }
}
