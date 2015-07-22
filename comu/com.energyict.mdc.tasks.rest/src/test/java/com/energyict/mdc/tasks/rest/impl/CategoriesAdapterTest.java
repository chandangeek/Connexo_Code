package com.energyict.mdc.tasks.rest.impl;

import com.energyict.mdc.tasks.rest.Categories;
import org.junit.Test;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by gde on 4/05/2015.
 */
public class CategoriesAdapterTest {

    @Test
    public void testCategoriesAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new CategoriesAdapter(), Categories.values());
    }

    private <C> void testAdapter(XmlAdapter<String, C> adapter, C[] values) throws Exception {
        for (C serverSideValue : values) {
            assertThat(adapter.marshal(serverSideValue)).describedAs("Unmapped server-side value detected in adapter "+adapter.getClass().getSimpleName()).isNotNull();
        }

    }
}
