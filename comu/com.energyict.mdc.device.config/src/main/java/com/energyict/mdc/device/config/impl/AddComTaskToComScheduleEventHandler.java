package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.energyict.mdc.common.rest.IdListBuilder;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.scheduling.events.UpdateEventType;
import com.energyict.mdc.scheduling.events.VetoComTaskAdditionException;
import com.energyict.mdc.scheduling.model.ComTaskComScheduleLink;
import com.energyict.mdc.tasks.ComTask;
import java.util.List;
import java.util.Map;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Copyrights EnergyICT
 * Date: 27/03/2014
 * Time: 10:37
 */
@Component(name="com.energyict.mdc.device.config.comtask.comschedule.eventhandler", service = TopicHandler.class, immediate = true)
public class AddComTaskToComScheduleEventHandler implements TopicHandler {

    private volatile DeviceConfigurationService deviceConfigurationService;

    @Override
    public void handle(LocalEvent localEvent) {
        ComTaskComScheduleLink source = (ComTaskComScheduleLink) localEvent.getSource();
        List<ComTask> comTasks = deviceConfigurationService.findAvailableComTasks(source.getComSchedule());
        Map<Long,ComTask> existingComTaskMap = IdListBuilder.asIdMap(source.getComSchedule().getComTasks());
        boolean found = false;
        for (ComTask comTask : comTasks) {
            if (comTask.getId()==source.getComTask().getId() && !existingComTaskMap.containsKey(source.getComTask().getId())) {
                found=true;
            }
        }
        if (!found) {
            throw new VetoComTaskAdditionException();
        }
    }

    @Override
    public String getTopicMatcher() {
        return UpdateEventType.COMTASK_WILL_BE_ADDED_TO_SCHEDULE.topic();
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }
}
