package com.energyict.mdc.tasks.rest.impl;

import org.junit.Test;

import com.jayway.jsonpath.JsonModel;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by gde on 5/05/2015.
 */
public class ComTaskResourceTest extends ComTasksApplicationJerseyTest {

    @Test
    public void testGetCategories() throws Exception {
        String response = target("/comtasks/categories").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(6);
        assertThat(jsonModel.<String>get("$.data[?(@.id=='logbooks')].name[0]")).isEqualTo(MessageSeeds.LOGBOOKS.getDefaultFormat());
        assertThat(jsonModel.<String>get("$.data[?(@.id=='registers')].name[0]")).isEqualTo(MessageSeeds.REGISTERS.getDefaultFormat());
        assertThat(jsonModel.<String>get("$.data[?(@.id=='topology')].name[0]")).isEqualTo(MessageSeeds.TOPOLOGY.getDefaultFormat());
        assertThat(jsonModel.<String>get("$.data[?(@.id=='loadprofiles')].name[0]")).isEqualTo(MessageSeeds.LOADPROFILES.getDefaultFormat());
        assertThat(jsonModel.<String>get("$.data[?(@.id=='clock')].name[0]")).isEqualTo(MessageSeeds.CLOCK.getDefaultFormat());
        assertThat(jsonModel.<String>get("$.data[?(@.id=='status')].name[0]")).isEqualTo(MessageSeeds.STATUS.getDefaultFormat());
    }
}
