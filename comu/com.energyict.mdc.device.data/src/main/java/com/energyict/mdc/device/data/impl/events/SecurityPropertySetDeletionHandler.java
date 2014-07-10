package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.impl.ServerDeviceDataService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Handles delete events that are being sent when a {@link SecurityPropertySet}
 * is about to be deleted and will veto the delete when it is in use by at least one device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-10 (15:04)
 */
@Component(name="com.energyict.mdc.device.data.delete.securitypropertyset.eventhandler", service = TopicHandler.class, immediate = true)
public class SecurityPropertySetDeletionHandler implements TopicHandler {

    static final String TOPIC = "com/energyict/mdc/device/config/securitypropertyset/VALIDATE_DELETE";

    private volatile ServerDeviceDataService deviceDataService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile Thesaurus thesaurus;

    public SecurityPropertySetDeletionHandler() {
        super();
    }

    // For testing purposes only
    SecurityPropertySetDeletionHandler(ProtocolPluggableService protocolPluggableService, ServerDeviceDataService deviceDataService) {
        this();
        this.setDeviceDataService(deviceDataService);
        this.setProtocolPluggableService(protocolPluggableService);
        this.thesaurus = deviceDataService.getThesaurus();
    }

    @Reference
    public void setDeviceDataService(DeviceDataService deviceDataService) {
        this.setDeviceDataService((ServerDeviceDataService) deviceDataService);
    }

    private void setDeviceDataService(ServerDeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
        this.thesaurus = deviceDataService.getThesaurus();
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
     * Vetos the delection of the {@link SecurityPropertySet}
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