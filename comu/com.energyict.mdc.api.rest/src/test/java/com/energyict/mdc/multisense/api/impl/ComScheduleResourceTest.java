package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.energyict.mdc.scheduling.model.ComSchedule;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ComScheduleResourceTest extends MultisensePublicApiJerseyTest {

    @Test
    public void testAllGetComSchedulesPaged() throws Exception {
        ComSchedule comSchedule1 = mockComSchedule(10, "schedule 1", 3333L);
        ComSchedule comSchedule2 = mockComSchedule(11, "schedule 2", 3333L);
        Finder<ComSchedule> finder = mockFinder(Arrays.asList(comSchedule1, comSchedule2));
        when(schedulingService.findAllSchedules()).thenReturn(finder);
        Response response = target("/comschedules").queryParam("start",0).queryParam("limit",10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/comschedules?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(2);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(10);
        assertThat(model.<String>get("data[0].name")).isEqualTo("schedule 1");
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/comschedules/10");
    }

    @Test
    public void testGetSingleComScheduleWithFields() throws Exception {
        ComSchedule comSchedule1 = mockComSchedule(31, "schedule 1", 3333L);
        Response response = target("/comschedules/31").queryParam("fields","id,name").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        assertThat(model.<String>get("$.name")).isEqualTo("schedule 1");
        assertThat(model.<String>get("$.link")).isNull();
    }



    @Test
    public void testComScheduleFields() throws Exception {
        Response response = target("/comschedules").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(10);
        assertThat(model.<List<String>>get("$")).containsOnly("comTasks","id","version","isInUse","link","mRID","name","plannedDate","startDate","temporalExpression");
    }


}
