package com.energyict.mdc.device.data.rest;

import com.energyict.mdc.engine.config.ComServer;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

public class AdapterTest {

    @Test
    public void testLogLevelAdapterTest() throws Exception {
        testAdapter(new LogLevelAdapter(), ComServer.LogLevel.values());

    }

    private <C> void testAdapter(XmlAdapter<String, C> adapter, C[] values) throws Exception {
        for (C serverSideValue : values) {
            assertThat(adapter.marshal(serverSideValue)).describedAs("Unmapped server-side value detected in adapter "+adapter.getClass().getSimpleName()).isNotNull();
        }

    }

}