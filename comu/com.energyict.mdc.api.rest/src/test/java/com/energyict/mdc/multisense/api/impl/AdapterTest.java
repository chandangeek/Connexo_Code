/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.data.tasks.TaskStatus;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AdapterTest {

    @Test
    public void testTaskStatusAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new TaskStatusAdapter(), TaskStatus.values());
    }

    private void testAdapter(XmlAdapter adapter, Object[] values) throws Exception {
        for (Object serverSideValue : values) {
            assertThat(adapter.marshal(serverSideValue)).describedAs("Unmapped server-side value detected in adapter "+adapter.getClass().getSimpleName()).isNotNull();
        }

    }

}
