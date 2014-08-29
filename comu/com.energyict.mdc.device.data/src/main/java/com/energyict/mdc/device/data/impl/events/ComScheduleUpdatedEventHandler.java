package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.data.impl.ServerDeviceDataService;
import com.energyict.mdc.scheduling.events.EventType;
import com.energyict.mdc.scheduling.events.VetoComTaskAdditionException;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.ComTaskComScheduleLink;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Responds to events that are produced when a {@link com.energyict.mdc.tasks.ComTask}
 * is being added to a {@link ComSchedule} and will veto that when
 * there is at least one Device that is linked to the ComSchedule.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-29 (13:44)
 */
@Component(name="com.energyict.mdc.device.comschedule.addComTask.eventhandler", service = TopicHandler.class, immediate = true)
public class ComScheduleUpdatedEventHandler implements TopicHandler {

    private volatile ServerDeviceDataService deviceDataService;

    @Override
    public String getTopicMatcher() {
        return EventType.COMTASK_WILL_BE_ADDED_TO_SCHEDULE.topic();
    }

    @Override
    public void handle(LocalEvent localEvent) {
        ComTaskComScheduleLink source = (ComTaskComScheduleLink) localEvent.getSource();
        ComSchedule comSchedule = source.getComSchedule();
        if (this.deviceDataService.isLinkedToDevices(comSchedule)) {
            throw new VetoComTaskAdditionException();
        }
    }

    @SuppressWarnings("unused")
    @Reference
    public void setDeviceDataService(ServerDeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }

}