package com.elster.jupiter.validation.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRuleSetVersionsResourceTest extends BaseValidationRestTest {

    @Test
    public void getCreateTasksTest() {

        Response response1 = target("/rulesetversions").request().get();
        assertThat(response1.getStatus()).isEqualTo(404);
    }

    @Test
    public void testDeleteRuleSetVersions(){
        Response response = target("/validation/1").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
    }

}