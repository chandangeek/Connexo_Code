package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.impl.ServerDeviceDataService;
import com.energyict.mdc.scheduling.model.ComSchedule;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pubsub.EventHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Handles delete events that are being sent when a {@link ComSchedule}
 * is about to be deleted and will veto the delete when it is in use by at least one device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-03 (14:27)
 */
@Component(name="com.energyict.mdc.device.data.delete.comschedule.eventhandler", service = TopicHandler.class, immediate = true)
public class ComScheduleDeletionHandler extends EventHandler<LocalEvent> {

    static final String TOPIC = "com/energyict/mdc/scheduling/comschedules/BEFORE_DELETE";

    private volatile ServerDeviceDataService deviceDataService;
    private volatile Thesaurus thesaurus;

    public ComScheduleDeletionHandler() {
        super(LocalEvent.class);
    }

    @Inject
    ComScheduleDeletionHandler(ServerDeviceDataService deviceDataService, Thesaurus thesaurus) {
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
            ComSchedule comSchedule = (ComSchedule) event.getSource();
            this.validateNotUsedByDevice(comSchedule);
        }
    }

    /**
     * Vetos the delection of the {@link ComSchedule}
     * by throwing an exception when the ComSchedule
     * is used by at least on Device, i.e. at least one
     * {@link com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution}
     * for that ComSchedule was created on that Device.
     *
     * @param comSchedule The ComTaskEnablement that is about to be deleted
     */
    private void validateNotUsedByDevice(ComSchedule comSchedule) {
        if (this.deviceDataService.hasComTaskExecutions(comSchedule)) {
            throw new VetoDeleteComScheduleException(this.thesaurus, comSchedule);
        }
    }

}