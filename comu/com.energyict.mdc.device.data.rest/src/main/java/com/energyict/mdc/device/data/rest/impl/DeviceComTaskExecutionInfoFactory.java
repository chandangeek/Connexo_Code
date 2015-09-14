package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.CollectionUtil;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfo.ConnectionStrategyInfo;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.util.List;
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
                info.connectionStrategy.id = scheduledConnectionTask.getConnectionStrategy().name();
                info.connectionStrategy.displayValue = ConnectionStrategyTranslationKeys.translationFor(scheduledConnectionTask.getConnectionStrategy(), getThesaurus());
            }
        });
    }

    private ComTask getComTask(ComTaskExecution comTaskExecution) {
        if (comTaskExecution.getComTasks().size() == 1) {
            return comTaskExecution.getComTasks().get(0);
        }
        List<ComTaskEnablement> comTaskEnablements = comTaskExecution.getDevice().getDeviceConfiguration().getComTaskEnablements();
        return comTaskEnablements.stream()
                .map(ComTaskEnablement::getComTask)
                .filter(ct -> CollectionUtil.contains(comTaskExecution.getComTasks(), ct))
                .findFirst()
                .orElse(null);
    }

}