package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.device.data.tasks.history.ComSession;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bvn on 8/29/14.
 */
public class SuccessIndicatorTaskCounterInfoComparator implements java.util.Comparator<TaskCounterInfo> {

    private Map<String, Integer> successIndicatorSortingMap = new HashMap<>(4);

    public SuccessIndicatorTaskCounterInfoComparator() {
        successIndicatorSortingMap.put(ComSession.SuccessIndicator.Success.name(), 1);
        successIndicatorSortingMap.put(TranslationKeys.SUCCESS_WITH_FAILED_TASKS.getKey(), 2);
        successIndicatorSortingMap.put(ComSession.SuccessIndicator.Broken.name(), 3);
        successIndicatorSortingMap.put(ComSession.SuccessIndicator.SetupError.name(), 4);
    }

    @Override
    public int compare(TaskCounterInfo taskCounterInfo1, TaskCounterInfo taskCounterInfo2) {
        return successIndicatorSortingMap.get(taskCounterInfo1.id).compareTo(successIndicatorSortingMap.get(taskCounterInfo2.id));
    }

}