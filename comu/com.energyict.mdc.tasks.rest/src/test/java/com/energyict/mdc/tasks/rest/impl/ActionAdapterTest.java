package com.energyict.mdc.tasks.rest.impl;

import org.junit.Test;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import static org.assertj.core.api.Assertions.assertThat;

public class ActionAdapterTest {
    @Test
    public void testCategoriesAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new ActionAdapter(), new String[]{ "read", "update", "verify", "set", "force", "synchronize" });
    }

    private <C> void testAdapter(XmlAdapter<String, C> adapter, C[] values) throws Exception {
        for (C serverSideValue : values) {
            assertThat(adapter.marshal(serverSideValue)).describedAs("Unmapped server-side value detected in adapter "+adapter.getClass().getSimpleName()).isNotNull();
        }
    }
}
