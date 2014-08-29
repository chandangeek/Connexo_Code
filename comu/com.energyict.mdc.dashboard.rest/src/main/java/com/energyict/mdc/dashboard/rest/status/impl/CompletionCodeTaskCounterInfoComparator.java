package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Can be used to sort a list of TaskCounterInfo according to the id (CompletionCode)
 * Created by bvn on 8/29/14.
 */
public class CompletionCodeTaskCounterInfoComparator implements Comparator<TaskCounterInfo> {

    private static final CompletionCodeAdapter completionCodeAdapter = new CompletionCodeAdapter();

    private Map<String, Integer> completionCodeSortingOrder = new HashMap<>();

    public CompletionCodeTaskCounterInfoComparator() {
        completionCodeSortingOrder.put(completionCodeAdapter.marshal(CompletionCode.Ok), 1);
        completionCodeSortingOrder.put(completionCodeAdapter.marshal(CompletionCode.ConfigurationError), 2);
        completionCodeSortingOrder.put(completionCodeAdapter.marshal(CompletionCode.ConfigurationWarning), 3);
        completionCodeSortingOrder.put(completionCodeAdapter.marshal(CompletionCode.ConnectionError), 4);
        completionCodeSortingOrder.put(completionCodeAdapter.marshal(CompletionCode.IOError), 5);
        completionCodeSortingOrder.put(completionCodeAdapter.marshal(CompletionCode.ProtocolError), 6);
        completionCodeSortingOrder.put(completionCodeAdapter.marshal(CompletionCode.Rescheduled), 7);
        completionCodeSortingOrder.put(completionCodeAdapter.marshal(CompletionCode.TimeError), 8);
        completionCodeSortingOrder.put(completionCodeAdapter.marshal(CompletionCode.UnexpectedError), 9);
    }

    @Override
    public int compare(TaskCounterInfo taskCounterInfo1, TaskCounterInfo taskCounterInfo2) {
        return completionCodeSortingOrder.get(taskCounterInfo1.id).compareTo(completionCodeSortingOrder.get(taskCounterInfo2.id));
    }
}
