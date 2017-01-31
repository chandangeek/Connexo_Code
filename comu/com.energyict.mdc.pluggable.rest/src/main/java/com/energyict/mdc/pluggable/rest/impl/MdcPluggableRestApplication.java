/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.impl.properties.CalendarPropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.ClockPropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.DatePropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.DeviceMessageFilePropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.Ean13PropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.Ean18PropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.FirmwareVersionPropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.HexStringPropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.LoadProfilePropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.LoadProfileTypePropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.LogbookPropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.ObisCodePropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.PasswordPropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.ReadingTypePropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.RegisterPropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.TimeDurationPropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.TimeOfDayPropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.TimeZoneInUsePropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.properties.UsagePointPropertyValueConverter;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.energyict.mdc.pluggable.rest", service = {Application.class, MessageSeedProvider.class}, immediate = true, property = {"alias=/plr", "app=MDC", "name=" + MdcPluggableRestApplication.COMPONENT_NAME})
public class MdcPluggableRestApplication extends Application implements MessageSeedProvider {
    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "PLR";

    private volatile CalendarService calendarService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile PropertySpecService propertySpecService;
    private volatile TransactionService transactionService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile License license;
    private volatile FirmwareService firmwareService;
    private volatile PropertyValueInfoService propertyValueInfoService;
    private NlsService nlsService;
    private Thesaurus thesaurus;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                DeviceCommunicationProtocolsResource.class,
                DeviceDiscoveryProtocolsResource.class,
                LicensedProtocolResource.class,
                TimeZoneInUseResource.class,
                DeviceMessageFileReferenceResource.class,
                LoadProfileTypeResource.class,
                CalendarResource.class);
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Activate
    public void activate() {
        propertyValueInfoService.addPropertyValueInfoConverter(new CalendarPropertyValueConverter());
        propertyValueInfoService.addPropertyValueInfoConverter(new ClockPropertyValueConverter());
        propertyValueInfoService.addPropertyValueInfoConverter(new DeviceMessageFilePropertyValueConverter());
        propertyValueInfoService.addPropertyValueInfoConverter(new Ean13PropertyValueConverter());
        propertyValueInfoService.addPropertyValueInfoConverter(new Ean18PropertyValueConverter());
        propertyValueInfoService.addPropertyValueInfoConverter(new FirmwareVersionPropertyValueConverter());
        propertyValueInfoService.addPropertyValueInfoConverter(new HexStringPropertyValueConverter());
        propertyValueInfoService.addPropertyValueInfoConverter(new LoadProfilePropertyValueConverter());
        propertyValueInfoService.addPropertyValueInfoConverter(new LoadProfileTypePropertyValueConverter());
        propertyValueInfoService.addPropertyValueInfoConverter(new LogbookPropertyValueConverter());
        propertyValueInfoService.addPropertyValueInfoConverter(new ObisCodePropertyValueConverter());
        propertyValueInfoService.addPropertyValueInfoConverter(new PasswordPropertyValueConverter());
        propertyValueInfoService.addPropertyValueInfoConverter(new ReadingTypePropertyValueConverter());
        propertyValueInfoService.addPropertyValueInfoConverter(new RegisterPropertyValueConverter());
        propertyValueInfoService.addPropertyValueInfoConverter(new TimeDurationPropertyValueConverter(thesaurus));
        propertyValueInfoService.addPropertyValueInfoConverter(new TimeOfDayPropertyValueConverter());
        propertyValueInfoService.addPropertyValueInfoConverter(new TimeZoneInUsePropertyValueConverter());
        propertyValueInfoService.addPropertyValueInfoConverter(new UsagePointPropertyValueConverter());
        propertyValueInfoService.addPropertyValueInfoConverter(new DatePropertyValueConverter());
    }

    @Reference
    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST)
                .join(nlsService.getThesaurus(TimeService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference(target="(com.elster.jupiter.license.rest.key=" + APP_KEY  + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    @Reference
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(calendarService).to(CalendarService.class);
            bind(protocolPluggableService).to(ProtocolPluggableService.class);
            bind(propertySpecService).to(PropertySpecService.class);
            bind(transactionService).to(TransactionService.class);
            bind(nlsService).to(NlsService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(deviceConfigurationService).to(DeviceConfigurationService.class);
            bind(MdcPropertyUtils.class).to(MdcPropertyUtils.class);
            bind(firmwareService).to(FirmwareService.class);
            bind(ResourceHelper.class).to(ResourceHelper.class);
            bind(propertyValueInfoService).to(PropertyValueInfoService.class);
        }
    }

}