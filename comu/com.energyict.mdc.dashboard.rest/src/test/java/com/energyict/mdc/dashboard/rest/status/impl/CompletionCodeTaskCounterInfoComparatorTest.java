/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.common.tasks.history.CompletionCode;

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Created by bvn on 8/29/14.
 */
public class CompletionCodeTaskCounterInfoComparatorTest {

    @Test
    public void testAllCodesCovered() throws Exception {
        CompletionCodeTaskCounterInfoComparator comparator = new CompletionCodeTaskCounterInfoComparator();
        for (CompletionCode completionCode : CompletionCode.values()) {
            try {
                TaskCounterInfo taskCounterInfo = new TaskCounterInfo();
                taskCounterInfo.id = completionCode.name();
                comparator.compare(taskCounterInfo, taskCounterInfo);
            } catch (NullPointerException e) {
                fail("CompletionCode "+completionCode+" is not supported by the comparator");
            }
        }
    }
}