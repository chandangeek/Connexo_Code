/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Handles delete events that are being sent when a {@link SecurityPropertySet}
 * is about to be deleted and will veto the delete when it is in use by at least one device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-10 (15:04)
 */
@Component(name = "com.energyict.mdc.device.data.delete.securitypropertyset.eventhandler", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class SecurityPropertySetDeletionHandler implements TopicHandler {

    private static final String TOPIC = EventType.SECURITY_PROPERTY_SET_VALIDATE_DELETE.topic();

    private volatile Clock clock;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile Thesaurus thesaurus;

    public SecurityPropertySetDeletionHandler() {
        super();
    }

    // For testing purposes only
    SecurityPropertySetDeletionHandler(Clock clock, CustomPropertySetService customPropertySetService, DeviceDataModelService deviceDataModelService) {
        this();
        this.setClock(clock);
        this.setDeviceDataModelService(deviceDataModelService);
        this.setCustomPropertySetService(customPropertySetService);
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setDeviceDataModelService(DeviceDataModelService deviceDataModelService) {
        this.thesaurus = deviceDataModelService.thesaurus();
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    @Override
    public void handle(LocalEvent event) {
        SecurityPropertySet securityPropertySet = (SecurityPropertySet) event.getSource();
        this.validateNotUsedByDevice(securityPropertySet);
    }

    /**
     * Vetos the deletion of the {@link SecurityPropertySet}
     * by throwing an exception when the SecurityPropertySet
     * is used by at least on Device, i.e. at least one
     * Relation that uses it on a Device.
     *
     * @param securityPropertySet The SecurityPropertySet that is about to be deleted
     */
    private void validateNotUsedByDevice(SecurityPropertySet securityPropertySet) {
        securityPropertySet.getDeviceConfiguration()
                .getDeviceType()
                .getDeviceProtocolPluggableClass()
                .ifPresent(deviceProtocolPluggableClass -> deviceProtocolPluggableClass.getDeviceProtocol()
                        .getCustomPropertySet()
                        .ifPresent(cps -> this.validateNotUsedByDevice(securityPropertySet, cps)));
    }

    /**
     * Vetos the deletion of the {@link SecurityPropertySet}
     * by throwing an exception when the SecurityPropertySet
     * is used by at least one Device, i.e. at least one
     * PersistentDomainExtension that references the Device.
     *
     * @param securityPropertySet The SecurityPropertySet that is about to be deleted
     */
    private void validateNotUsedByDevice(SecurityPropertySet securityPropertySet, CustomPropertySet customPropertySet) {
        Condition condition = where(CommonBaseDeviceSecurityProperties.Fields.PROPERTY_SPEC_PROVIDER.javaName()).isEqualTo(securityPropertySet);
        List valuesEntities = this.customPropertySetService
                .getVersionedValuesEntitiesFor(customPropertySet)
                .matching(condition)
                .andEffectiveAt(this.clock.instant());
        if (!valuesEntities.isEmpty()) {
            throw new VetoDeleteSecurityPropertySetException(this.thesaurus, securityPropertySet);
        }
    }
}