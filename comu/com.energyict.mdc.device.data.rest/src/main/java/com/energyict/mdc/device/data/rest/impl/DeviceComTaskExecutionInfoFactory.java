/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfo.ConnectionStrategyInfo;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Supplier;

public class DeviceComTaskExecutionInfoFactory extends BaseComTaskExecutionInfoFactory<DeviceComTaskExecutionInfo> {

    @Inject
    public DeviceComTaskExecutionInfoFactory(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    protected Supplier<DeviceComTaskExecutionInfo> getInfoSupplier() {
        return DeviceComTaskExecutionInfo::new;
    }

    @Override
    protected void initExtraFields(DeviceComTaskExecutionInfo info, ComTaskExecution comTaskExecution, Optional<ComTaskExecutionSession> comTaskExecutionSession) {
        info.id = comTaskExecution.getId();
        info.comTask = new IdWithNameInfo(getComTask(comTaskExecution));
        info.isOnHold = comTaskExecution.isOnHold();
        info.plannedDate = comTaskExecution.getNextExecutionTimestamp();
        comTaskExecution.getConnectionTask().ifPresent(connectionTask -> {
            info.connectionMethod = connectionTask.getPartialConnectionTask().getName();
            if (connectionTask.isDefault()) {
                info.connectionMethod += " (" + getThesaurus().getFormat(DefaultTranslationKey.DEFAULT).format() + ")";
            }
            if (connectionTask instanceof ScheduledConnectionTask) {
                ScheduledConnectionTask scheduledConnectionTask = (ScheduledConnectionTask) connectionTask;
                info.connectionStrategy = new ConnectionStrategyInfo();
                info.connectionStrategy.connectionStrategy = scheduledConnectionTask.getConnectionStrategy().name();
                info.connectionStrategy.localizedValue = ConnectionStrategyTranslationKeys.translationFor(scheduledConnectionTask.getConnectionStrategy(), getThesaurus());
            }
        });
    }

    private ComTask getComTask(ComTaskExecution comTaskExecution) {
        return comTaskExecution.getComTask();
    }

}