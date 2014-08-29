package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.device.data.tasks.history.ComSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bvn on 8/29/14.
 */
public class SuccessIndicatorTaskCounterInfoComparator implements java.util.Comparator<TaskCounterInfo> {

    private static final SuccessIndicatorAdapter successIndicatorAdaptor = new SuccessIndicatorAdapter();

    private Map<String, Integer> successIndicatorSortingMap = new HashMap<>(4);

    public SuccessIndicatorTaskCounterInfoComparator() {
        successIndicatorSortingMap.put(successIndicatorAdaptor.marshal(ComSession.SuccessIndicator.Success), 1);
        successIndicatorSortingMap.put(MessageSeeds.AT_LEAST_ONE_FAILED.getKey(), 2);
        successIndicatorSortingMap.put(successIndicatorAdaptor.marshal(ComSession.SuccessIndicator.Broken), 3);
        successIndicatorSortingMap.put(successIndicatorAdaptor.marshal(ComSession.SuccessIndicator.SetupError), 4);
    }

    @Override
    public int compare(TaskCounterInfo taskCounterInfo1, TaskCounterInfo taskCounterInfo2) {
        return successIndicatorSortingMap.get(taskCounterInfo1.id).compareTo(successIndicatorSortingMap.get(taskCounterInfo2.id));
    }
}
