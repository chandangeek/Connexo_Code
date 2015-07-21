package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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

    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile Thesaurus thesaurus;

    public SecurityPropertySetDeletionHandler() {
        super();
    }

    // For testing purposes only
    SecurityPropertySetDeletionHandler(ProtocolPluggableService protocolPluggableService, DeviceDataModelService deviceDataModelService) {
        this();
        this.setDeviceDataModelService(deviceDataModelService);
        this.setProtocolPluggableService(protocolPluggableService);
    }

    @Reference
    public void setDeviceDataModelService(DeviceDataModelService deviceDataModelService) {
        this.thesaurus = deviceDataModelService.thesaurus();
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
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
        DeviceProtocol protocol = this.getDeviceProtocol(securityPropertySet);
        if (this.protocolPluggableService.hasSecurityRelations(securityPropertySet, protocol)) {
            throw new VetoDeleteSecurityPropertySetException(this.thesaurus, securityPropertySet);
        }
    }

    private DeviceProtocol getDeviceProtocol(SecurityPropertySet securityPropertySet) {
        return securityPropertySet.getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol();
    }

}