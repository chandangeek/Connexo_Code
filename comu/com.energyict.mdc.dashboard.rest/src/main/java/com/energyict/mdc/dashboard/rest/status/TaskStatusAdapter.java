package com.energyict.mdc.dashboard.rest.status;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.device.data.tasks.TaskStatus;

/**
 * Maps TaskStatus to related REST message seed
 * Created by bvn on 7/30/14.
 */
public class TaskStatusAdapter extends MapBasedXmlAdapter<TaskStatus> {

    public TaskStatusAdapter() {
        register(MessageSeeds.BUSY.getKey(), TaskStatus.Busy);
        register(MessageSeeds.FAILED.getKey(), TaskStatus.Failed);
        register(MessageSeeds.NEVER_COMPLETED.getKey(), TaskStatus.NeverCompleted);
        register(MessageSeeds.ON_HOLD.getKey(), TaskStatus.OnHold);
        register(MessageSeeds.PENDING.getKey(), TaskStatus.Pending);
        register(MessageSeeds.RETRYING.getKey(), TaskStatus.Retrying);
        register(MessageSeeds.WAITING.getKey(), TaskStatus.Waiting);
    }
}
