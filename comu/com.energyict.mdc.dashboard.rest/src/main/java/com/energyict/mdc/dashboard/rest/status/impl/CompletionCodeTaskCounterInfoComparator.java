package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.device.data.tasks.history.CompletionCode;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Can be used to sort a list of TaskCounterInfo according to the id (CompletionCode)
 * Created by bvn on 8/29/14.
 */
public class CompletionCodeTaskCounterInfoComparator implements Comparator<TaskCounterInfo> {

    private Map<String, Integer> completionCodeSortingOrder = new HashMap<>();

    public CompletionCodeTaskCounterInfoComparator() {
        Stream
            .of(CompletionCode.values())
            .forEach(completionCode -> completionCodeSortingOrder.put(completionCode.name(), completionCode.ordinal()));
    }

    @Override
    public int compare(TaskCounterInfo taskCounterInfo1, TaskCounterInfo taskCounterInfo2) {
        return completionCodeSortingOrder.get(taskCounterInfo1.id).compareTo(completionCodeSortingOrder.get(taskCounterInfo2.id));
    }

}