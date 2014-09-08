package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AdapterTest {

    @Test
    public void testCompletionCodeAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new CompletionCodeAdapter(), CompletionCode.values());
    }

    @Test
    public void testSuccessIndicatorAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new ComSessionSuccessIndicatorAdapter(), ComSession.SuccessIndicator.values());
    }

    private <C> void testAdapter(XmlAdapter<String, C> adapter, C[] values) throws Exception {
        for (C serverSideValue : values) {
            assertThat(adapter.marshal(serverSideValue)).describedAs("Unmapped server-side value detected in adapter "+adapter.getClass().getSimpleName()).isNotNull();
        }

    }

}
