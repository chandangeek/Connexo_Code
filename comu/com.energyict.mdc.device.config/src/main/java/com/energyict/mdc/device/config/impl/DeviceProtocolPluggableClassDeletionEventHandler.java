package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.exceptions.VetoDeviceProtocolPluggableClassDeletionBecauseStillUsedByDeviceTypesException;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * Handles delete events that are being sent when a {@link DeviceProtocolPluggableClass}
 * is being deleted and will check if a {@link DeviceType} is using it.
 * If that is the case, the delete will be vetoed by throwing an exception.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-19 (12:42)
 */
@Component(name="com.energyict.mdc.device.config.protocol.delete.eventhandler", service = TopicHandler.class, immediate = true)
public class DeviceProtocolPluggableClassDeletionEventHandler implements TopicHandler {

    private static final String TOPIC = "com/energyict/mdc/protocol/pluggable/deviceprotocol/DELETED";

    private volatile Thesaurus thesaurus;
    private volatile DeviceConfigurationService deviceConfigurationService;

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    @Override
    public void handle(LocalEvent event) {
        this.handleDeleteProtocolPluggableClass((DeviceProtocolPluggableClass) event.getSource());
    }

    private void handleDeleteProtocolPluggableClass(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        List<DeviceType> deviceTypes = this.deviceConfigurationService.findDeviceTypesWithDeviceProtocol(deviceProtocolPluggableClass);
        if (!deviceTypes.isEmpty()) {
            throw new VetoDeviceProtocolPluggableClassDeletionBecauseStillUsedByDeviceTypesException(deviceProtocolPluggableClass, deviceTypes, this.thesaurus, MessageSeeds.VETO_DEVICEPROTOCOLPLUGGABLECLASS_DELETION);
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.setThesaurus(nlsService.getThesaurus(DeviceConfigurationService.COMPONENTNAME, Layer.DOMAIN));
    }

    private void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

}