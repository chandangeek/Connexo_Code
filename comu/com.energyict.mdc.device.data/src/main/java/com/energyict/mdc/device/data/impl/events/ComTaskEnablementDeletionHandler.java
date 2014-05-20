package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pubsub.EventHandler;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.impl.ServerDeviceDataService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Handles delete events that are being sent when a {@link ComTaskEnablement}
 * is about to be deleted and will veto the delete when it is in use by at least one device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-24 (11:51)
 */
@Component(name="com.energyict.mdc.device.data.delete.comtaskenablement.eventhandler", service = TopicHandler.class, immediate = true)
public class ComTaskEnablementDeletionHandler extends EventHandler<LocalEvent> {

    static final String TOPIC = "com/energyict/mdc/device/config/comtaskenablement/VALIDATEDELETE";

    private volatile ServerDeviceDataService deviceDataService;
    private volatile Thesaurus thesaurus;

    public ComTaskEnablementDeletionHandler() {
        super(LocalEvent.class);
    }

    @Inject
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
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        if (event.getType().getTopic().equals(TOPIC)) {
            ComTaskEnablement comTaskEnablement = (ComTaskEnablement) event.getSource();
            this.validateNotUsedByDevice(comTaskEnablement);
        }
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