package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.energyict.mdc.tasks.ComTask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by bvn on 8/12/14.
 */
public class ComTaskExecutionInfo {
    private static final Comparator<ComTaskExecution> COM_TASK_EXECUTION_COMPARATOR = new ComTaskExecutionComparator();

    public String name;
    public IdWithNameInfo device;
    public IdWithNameInfo deviceConfiguration;
    public IdWithNameInfo deviceType;
    public String comScheduleName;
    public TemporalExpressionInfo comScheduleFrequency;
    public int urgency;
    public TaskStatusInfo currentState;
    public Date startTime;
    public Date successfulFinishTime;
    public Date nextCommunication;
    public boolean alwaysExecuteOnInbound;

    public static ComTaskExecutionInfo from(ComTaskExecution comTaskExecution, Thesaurus thesaurus) throws Exception {
        ComTaskExecutionInfo info = new ComTaskExecutionInfo();
        info.name=buildName(comTaskExecution.getComTasks());
        info.device=new IdWithNameInfo(comTaskExecution.getDevice().getmRID(), comTaskExecution.getDevice().getName());
        info.deviceConfiguration=new IdWithNameInfo(comTaskExecution.getDevice());
        info.deviceType=new IdWithNameInfo(comTaskExecution.getDevice().getDeviceType());
        if (comTaskExecution instanceof ScheduledComTaskExecution) {
            ComSchedule comSchedule = ((ScheduledComTaskExecution) comTaskExecution).getComSchedule();
            info.comScheduleName=comSchedule.getName();
            if (comSchedule.getTemporalExpression()!=null) {
                info.comScheduleFrequency = TemporalExpressionInfo.from(comSchedule.getTemporalExpression());
            }
        }
        info.urgency = comTaskExecution.getExecutionPriority();
        info.currentState=new TaskStatusInfo(comTaskExecution.getStatus(), thesaurus);
        info.startTime=comTaskExecution.getLastExecutionStartTimestamp();
        info.successfulFinishTime=comTaskExecution.getLastSuccessfulCompletionTimestamp();
        info.nextCommunication=comTaskExecution.getNextExecutionTimestamp();
        info.alwaysExecuteOnInbound = comTaskExecution.isIgnoreNextExecutionSpecsForInbound();

        return info;
    }

    /**
     * Turns list of names A,B,C into "A + B + C"
     * @param comTasks
     * @return
     */

    private static String buildName(List<ComTask> comTasks) {
        StringBuilder name = new StringBuilder();
        for (ComTask comTask : comTasks) {
            if (name.length()>0) {
                name.append(" + ");
            }
            name.append(comTask.getName());
        }

        return null;
    }

    public static List<ComTaskExecutionInfo> from(List<ComTaskExecution> comTaskExecutions, Thesaurus thesaurus) throws Exception {
        List<ComTaskExecutionInfo> comTaskExecutionInfos = new ArrayList<>(comTaskExecutions.size());
        Collections.sort(comTaskExecutions, COM_TASK_EXECUTION_COMPARATOR);
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            comTaskExecutionInfos.add(ComTaskExecutionInfo.from(comTaskExecution, thesaurus));
        }
        return comTaskExecutionInfos;
    }
}
