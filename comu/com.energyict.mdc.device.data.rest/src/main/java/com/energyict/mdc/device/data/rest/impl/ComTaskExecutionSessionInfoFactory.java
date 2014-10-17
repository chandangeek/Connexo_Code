package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.google.common.base.Optional;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 10/6/14.
 */
public class ComTaskExecutionSessionInfoFactory {

    private final Thesaurus thesaurus;

    @Inject
    public ComTaskExecutionSessionInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public ComTaskExecutionSessionInfo from(ComTaskExecutionSession comTaskExecutionSession) {
        ComTaskExecutionSessionInfo info = new ComTaskExecutionSessionInfo();
        Device device = comTaskExecutionSession.getDevice();
        ComTaskExecution comTaskExecution = comTaskExecutionSession.getComTaskExecution();
        info.comTasks = comTaskExecution.getComTasks().stream().sorted((c1,c2)->c1.getName().compareToIgnoreCase(c2.getName())).map(IdWithNameInfo::new).collect(toList());
        info.name = String.join(" + ", info.comTasks.stream().map(i -> i.name).collect(toList()));
        info.device = new IdWithNameInfo(device.getmRID(), device.getName());
        info.deviceConfiguration = new IdWithNameInfo(device.getDeviceConfiguration());
        info.deviceType = new IdWithNameInfo(device.getDeviceType());
        if (comTaskExecution instanceof ScheduledComTaskExecution) {
            ComSchedule comSchedule = ((ScheduledComTaskExecution) comTaskExecution).getComSchedule();
            info.comScheduleName=comSchedule.getName();
            if (comSchedule.getTemporalExpression()!=null) {
                info.comScheduleFrequency = TemporalExpressionInfo.from(comSchedule.getTemporalExpression());
            }
        } else {
            if (comTaskExecution instanceof ManuallyScheduledComTaskExecution) {
                Optional<NextExecutionSpecs> nextExecutionSpecs = comTaskExecution.getNextExecutionSpecs();
                info.comScheduleName=thesaurus.getString(MessageSeeds.INDIVIDUAL.getKey(), MessageSeeds.INDIVIDUAL.getKey());
                if (nextExecutionSpecs.isPresent()) {
                    info.comScheduleFrequency = TemporalExpressionInfo.from(nextExecutionSpecs.get().getTemporalExpression());
                }
            }
        }
        info.urgency = comTaskExecution.getExecutionPriority();
        MessageSeeds successIndicatorSeed = comTaskExecutionSession.getSuccessIndicator().equals(ComTaskExecutionSession.SuccessIndicator.Success) ?
                MessageSeeds.SUCCESS : MessageSeeds.FAILURE;
        info.result=thesaurus.getString(successIndicatorSeed.getKey(), successIndicatorSeed.getDefaultFormat());
        info.startTime=comTaskExecutionSession.getStartDate();
        info.finishTime =comTaskExecutionSession.getStopDate();
        info.alwaysExecuteOnInbound = comTaskExecution.isIgnoreNextExecutionSpecsForInbound();

        return info;
    }
}

class ComTaskExecutionSessionInfo {
    public String name;
    public List<IdWithNameInfo> comTasks;
    public IdWithNameInfo device;
    public IdWithNameInfo deviceConfiguration;
    public IdWithNameInfo deviceType;
    public String comScheduleName;
    public TemporalExpressionInfo comScheduleFrequency;
    public int urgency;
    public String result;
    public Date startTime;
    public Date finishTime;
    public boolean alwaysExecuteOnInbound;
}
