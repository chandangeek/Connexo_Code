package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.rest.ComSessionSuccessIndicatorAdapter;
import com.energyict.mdc.device.data.rest.SuccessIndicatorInfo;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import javax.inject.Inject;

/**
 * Created by bvn on 10/3/14.
 */
public class ComSessionInfoFactory {
    private final ComSessionSuccessIndicatorAdapter comSessionSuccessIndicatorAdapter = new ComSessionSuccessIndicatorAdapter();
    private final Thesaurus thesaurus;

    @Inject
    public ComSessionInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public ComSessionInfo from(ComSession comSession) {
        ComSessionInfo info = new ComSessionInfo();
        ConnectionTask<?,?> connectionTask = comSession.getConnectionTask();
        PartialConnectionTask partialConnectionTask = connectionTask.getPartialConnectionTask();

        info.connectionMethod = connectionTask.getName();
        info.isDefault = connectionTask.isDefault();
        info.startedOn = comSession.getStartDate();
        info.finishedOn = comSession.getStopDate();
        info.durationInSeconds = comSession.getTotalDuration().getStandardSeconds();
        if (comSession.getComPort()!=null) {
            info.comPort = comSession.getComPort().getName();
            info.comServer = new IdWithNameInfo(comSession.getComPort().getComServer());
        }
        String direction = partialConnectionTask.getConnectionType().getDirection().name();
        info.direction = thesaurus.getString(direction, direction);
        info.connectionType = partialConnectionTask.getPluggableClass().getName();
        info.status = comSession.getSuccessIndicator().equals(ComSession.SuccessIndicator.Success)?
                thesaurus.getString(MessageSeeds.SUCCESS.getKey(), "Success"):
                thesaurus.getString(MessageSeeds.FAILURE.getKey(), "Failure");

        info.result = new SuccessIndicatorInfo(comSession.getSuccessIndicator(), thesaurus);
        info.comTaskCount = new ComTaskCountInfo();
        info.comTaskCount.numberOfSuccessfulTasks = comSession.getNumberOfSuccessFulTasks();
        info.comTaskCount.numberOfFailedTasks = comSession.getNumberOfFailedTasks();
        info.comTaskCount.numberOfIncompleteTasks = comSession.getNumberOfPlannedButNotExecutedTasks();
        return info;
    }

}


