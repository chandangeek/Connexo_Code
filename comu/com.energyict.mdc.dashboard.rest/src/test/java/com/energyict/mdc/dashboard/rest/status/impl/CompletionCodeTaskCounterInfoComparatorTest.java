package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Created by bvn on 8/29/14.
 */
public class CompletionCodeTaskCounterInfoComparatorTest {

    CompletionCodeAdapter completionCodeAdapter = new CompletionCodeAdapter();

    @Test
    public void testAllCodesCovered() throws Exception {
        CompletionCodeTaskCounterInfoComparator comparator = new CompletionCodeTaskCounterInfoComparator();
        for (CompletionCode completionCode : CompletionCode.values()) {
            try {
                TaskCounterInfo taskCounterInfo = new TaskCounterInfo();
                taskCounterInfo.id=completionCodeAdapter.marshal(completionCode);
                comparator.compare(taskCounterInfo, taskCounterInfo);
            } catch (NullPointerException e) {
                fail("CompletionCode "+completionCode+" is not supported by the comparator");
            }
        }
    }
}
