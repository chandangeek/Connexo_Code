package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.domain.util.Finder;
import com.energyict.mdc.tasks.ProtocolTask;
import com.jayway.jsonpath.JsonModel;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.Response;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 7/20/15.
 */
public class ProtocolTaskResourceTest extends MultisensePublicApiJerseyTest {

    @Test
    public void testAllGetProtocolTasksPaged() throws Exception {
        ProtocolTask protocolTask1 = mockClockTask(3);
        Finder<ProtocolTask> protocolTaskFinder = mockFinder(Arrays.asList(protocolTask1));
        when(taskService.findAllProtocolTasks()).thenReturn(protocolTaskFinder);
        Response response = target("/commands").queryParam("start",0).queryParam("limit",10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/commands?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(1);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(3);
        assertThat(model.<String>get("data[0].category")).isEqualTo("clock");
        assertThat(model.<String>get("data[0].action")).isEqualTo("set");
    }
}
