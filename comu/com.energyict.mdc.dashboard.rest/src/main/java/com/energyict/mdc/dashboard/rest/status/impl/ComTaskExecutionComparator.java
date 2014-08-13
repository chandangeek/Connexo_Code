package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import java.util.Comparator;

/**
 * Created by bvn on 8/13/14.
 */
public class ComTaskExecutionComparator implements Comparator<ComTaskExecution> {
    @Override
    public int compare(ComTaskExecution o1, ComTaskExecution o2) {
        return o1.getDevice().getName().compareToIgnoreCase(o2.getDevice().getName());
    }

}
