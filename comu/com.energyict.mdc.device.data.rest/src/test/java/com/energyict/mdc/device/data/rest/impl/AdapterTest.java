package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AdapterTest {

    @Test
    public void testTaskStatusAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new TaskStatusAdapter(), TaskStatus.values());
    }

    private <C> void testAdapter(XmlAdapter<String, C> adapter, C[] values) throws Exception {
        for (C serverSideValue : values) {
            assertThat(adapter.marshal(serverSideValue)).describedAs("Unmapped server-side value detected in adapter "+adapter.getClass().getSimpleName()).isNotNull();
        }

    }

}
