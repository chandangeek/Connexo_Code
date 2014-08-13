package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.scheduling.model.ComSchedule;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by bvn on 8/12/14.
 */
public class ComTaskExecutionInfo {
    public String name;
    public IdWithNameInfo device;
    public IdWithNameInfo deviceConfiguration;
    public IdWithNameInfo deviceType;
    public String comScheduleName;
    public String comScheduleFrequency;
    public int urgency;
    public TaskStatusInfo currentState;
    public Date startTime;
    public Date successfulFinishTime;
    public Date nextCommunication;

    public static ComTaskExecutionInfo from(ComTaskExecution comTaskExecution, Thesaurus thesaurus) throws Exception {
        ComTaskExecutionInfo info = new ComTaskExecutionInfo();
        info.name=null;
        info.device=new IdWithNameInfo(comTaskExecution.getDevice().getmRID(), comTaskExecution.getDevice().getName());
        info.deviceConfiguration=new IdWithNameInfo(comTaskExecution.getDevice().getDeviceConfiguration().getId(), comTaskExecution.getDevice().getDeviceConfiguration().getName());
        info.deviceType=new IdWithNameInfo(comTaskExecution.getDevice().getDeviceType().getId(), comTaskExecution.getDevice().getDeviceType().getName());
        if (comTaskExecution instanceof ScheduledComTaskExecution) {
            ComSchedule comSchedule = ((ScheduledComTaskExecution) comTaskExecution).getComSchedule();
            info.comScheduleName=comSchedule.getName();
            info.comScheduleFrequency=comSchedule.getTemporalExpression().toString();
        }
        info.urgency = comTaskExecution.getExecutionPriority();
        info.currentState=new TaskStatusInfo(comTaskExecution.getStatus(), thesaurus);
        info.startTime=comTaskExecution.getLastExecutionStartTimestamp();
        info.successfulFinishTime=comTaskExecution.getLastSuccessfulCompletionTimestamp();
        info.nextCommunication=comTaskExecution.getNextExecutionTimestamp();
        return info;
    }

    public static List<ComTaskExecutionInfo> from(List<ComTaskExecution> comTaskExecutions, Thesaurus thesaurus) throws Exception {
        List<ComTaskExecutionInfo> comTaskExecutionInfos = new ArrayList<>(comTaskExecutions.size());
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            comTaskExecutionInfos.add(ComTaskExecutionInfo.from(comTaskExecution, thesaurus));
        }
        // TODO: SORT
        return comTaskExecutionInfos;
    }
}
