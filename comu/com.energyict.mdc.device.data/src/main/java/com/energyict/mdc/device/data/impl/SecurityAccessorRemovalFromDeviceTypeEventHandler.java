/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.EventType;
import com.energyict.mdc.common.device.config.SecurityAccessorTypeOnDeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.impl.pki.AbstractDeviceSecurityAccessorImpl;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Listens for validate-delete events for {@link SecurityAccessorType} on {@link DeviceType}
 * and will veto the deletion if the {@link SecurityAccessorType} is still referenced by any {@link SecurityAccessor}.
 */
@Component(name="com.energyict.mdc.device.data.impl.SecurityAccessorRemovalFromDeviceTypeEventHandler", service = TopicHandler.class, immediate = true)
public class SecurityAccessorRemovalFromDeviceTypeEventHandler implements TopicHandler {
    private volatile DeviceDataModelService deviceDataModelService;
    private volatile Thesaurus thesaurus;

    // OSGi
    public SecurityAccessorRemovalFromDeviceTypeEventHandler() {
        super();
    }

    // For testing purposes only
    @Inject
    public SecurityAccessorRemovalFromDeviceTypeEventHandler(DeviceDataModelService deviceDataModelService, NlsService nlsService) {
        this();
        setDeviceDataModelService(deviceDataModelService);
        setNlsService(nlsService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        SecurityAccessorTypeOnDeviceType source = (SecurityAccessorTypeOnDeviceType) localEvent.getSource();
        try(QueryStream<SecurityAccessor> stream = deviceDataModelService.dataModel().stream(SecurityAccessor.class)) {
            if(stream.join(Device.class)
                    .filter(Where.where(AbstractDeviceSecurityAccessorImpl.Fields.DEVICE.fieldName() + ".deviceType").isEqualTo(source.getDeviceType()))
                    .filter(Where.where(AbstractDeviceSecurityAccessorImpl.Fields.KEY_ACCESSOR_TYPE.fieldName()).isEqualTo(source.getSecurityAccessorType()))
                    .findAny()
                    .isPresent()) {
                throw new LocalizedException(thesaurus, MessageSeeds.VETO_SECURITY_ACCESSOR_REMOVAL_FROM_DEVICE_TYPE) {};
            }
        }
    }

    @Override
    public String getTopicMatcher() {
        return EventType.SECURITY_ACCESSOR_TYPE_VALIDATE_DELETE.topic();
    }

    @Reference
    public void setDeviceDataModelService(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }
}
