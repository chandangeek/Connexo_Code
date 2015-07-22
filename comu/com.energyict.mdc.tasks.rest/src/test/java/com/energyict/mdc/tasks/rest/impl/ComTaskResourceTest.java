package com.energyict.mdc.tasks.rest.impl;

import com.energyict.mdc.tasks.rest.Categories;
import org.junit.Test;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.util.List;

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
        assertThat(jsonModel.<String>get("$.data[?(@.id=='statusInformation')].name[0]")).isEqualTo(MessageSeeds.STATUS_INFORMATION.getDefaultFormat());
    }

    @Test
    public void testLoadProfilesActions() throws Exception {
        String response = target("/comtasks/actions").queryParam("category", Categories.LOADPROFILES.getId()).request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.data[?(@.id=='read')].name[0]")).isEqualTo("Read");
    }

    @Test
    public void testLogbooksActions() throws Exception {
        String response = target("/comtasks/actions").queryParam("category", Categories.LOGBOOKS.getId()).request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.data[?(@.id=='read')].name[0]")).isEqualTo("Read");
    }

    @Test
    public void testRegisterActions() throws Exception {
        String response = target("/comtasks/actions").queryParam("category", Categories.REGISTERS.getId()).request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.data[?(@.id=='read')].name[0]")).isEqualTo("Read");
    }

    @Test
    public void testTopologyActions() throws Exception {
        String response = target("/comtasks/actions").queryParam("category", Categories.TOPOLOGY.getId()).request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List>get("$.data[*].id")).contains("update", "verify").hasSize(2);
        assertThat(jsonModel.<List>get("$.data[*].name")).contains("Update", "Verify").hasSize(2);
        assertThat(jsonModel.<String>get("$.data[?(@.id=='update')].name[0]")).isEqualTo("Update");
    }

    @Test
    public void testClockActions() throws Exception {
        String response = target("/comtasks/actions").queryParam("category", Categories.CLOCK.getId()).request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List>get("$.data[*].id")).contains("set", "force", "synchronize").hasSize(3);
        assertThat(jsonModel.<List>get("$.data[*].name")).contains("Set", "Force", "Synchronize").hasSize(3);
        assertThat(jsonModel.<String>get("$.data[?(@.id=='set')].name[0]")).isEqualTo("Set");
        assertThat(jsonModel.<String>get("$.data[?(@.id=='force')].name[0]")).isEqualTo("Force");
    }

    @Test
    public void testStatusInformationActions() throws Exception {
        String response = target("/comtasks/actions").queryParam("category", Categories.STATUSINFORMATION.getId()).request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.data[?(@.id=='read')].name[0]")).isEqualTo("Read");
    }

    @Test
    public void testGetActionsForNonexistingCategory() throws Exception {
        final Response response = target("/comtasks/actions").queryParam("category", "Nonexisting").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetActionsWithoutCategory() throws Exception {
        final Response response = target("/comtasks/actions").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }
}
