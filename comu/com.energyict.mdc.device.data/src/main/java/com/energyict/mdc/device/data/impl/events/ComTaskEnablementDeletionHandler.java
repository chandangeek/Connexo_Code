package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.impl.ServerDeviceDataService;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Handles delete events that are being sent when a {@link ComTaskEnablement}
 * is about to be deleted and will veto the delete when it is in use by at least one device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-24 (11:51)
 */
@Component(name="com.energyict.mdc.device.data.delete.comtaskenablement.eventhandler", service = TopicHandler.class, immediate = true)
public class ComTaskEnablementDeletionHandler implements TopicHandler {

    static final String TOPIC = "com/energyict/mdc/device/config/comtaskenablement/VALIDATEDELETE";

    private volatile ServerDeviceDataService deviceDataService;
    private volatile Thesaurus thesaurus;

    public ComTaskEnablementDeletionHandler() {
        super();
    }

    // For testing purposes only
    ComTaskEnablementDeletionHandler (ServerDeviceDataService deviceDataService, Thesaurus thesaurus) {
        this();
        this.deviceDataService = deviceDataService;
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setDeviceDataService(DeviceDataService deviceDataService) {
        this.deviceDataService = (ServerDeviceDataService) deviceDataService;
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }

    @Override
    public void handle(LocalEvent event) {
        ComTaskEnablement comTaskEnablement = (ComTaskEnablement) event.getSource();
        this.validateNotUsedByDevice(comTaskEnablement);
    }

    /**
     * Vetos the delection of the {@link ComTaskEnablement}
     * by throwing an exception when the ComTaskEnablement
     * is used by at least on Device, i.e. at least one
     * ComTaskExecution that uses it on a Device.
     *
     * @param comTaskEnablement The ComTaskEnablement that is about to be deleted
     */
    private void validateNotUsedByDevice(ComTaskEnablement comTaskEnablement) {
        if (this.deviceDataService.hasComTaskExecutions(comTaskEnablement)) {
            throw new VetoDeleteComTaskEnablementException(this.thesaurus, comTaskEnablement);
        }
    }

}