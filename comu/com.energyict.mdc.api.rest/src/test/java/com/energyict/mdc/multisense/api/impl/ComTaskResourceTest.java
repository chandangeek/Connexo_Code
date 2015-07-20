package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.domain.util.Finder;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.jayway.jsonpath.JsonModel;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 7/20/15.
 */
public class ComTaskResourceTest extends MultisensePublicApiJerseyTest {

    @Test
    public void testAllGetComTasksPaged() throws Exception {
        DeviceMessageCategory cat4 = mockDeviceMessageCategory(111, "cat4");
        MessagesTask protocolTask = mock(MessagesTask.class);
        when(protocolTask.getDeviceMessageCategories()).thenReturn(Collections.singletonList(cat4));
        ComTask comTask31 = mockComTask(31, "Last");
        when(comTask31.getProtocolTasks()).thenReturn(Collections.singletonList(protocolTask));
        ComTask comTask32 = mockComTask(32, "Avante");
        when(comTask32.getProtocolTasks()).thenReturn(Collections.emptyList());
        Finder<ComTask> comTaskFinder = mockFinder(Arrays.asList(comTask31, comTask32));
        when(taskService.findAllComTasks()).thenReturn(comTaskFinder);
        Response response = target("/comtasks").queryParam("start",0).queryParam("limit",10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/comtasks?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(2);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(31);
        assertThat(model.<String>get("data[0].name")).isEqualTo("Last");
        assertThat(model.<List>get("data[0].categories")).hasSize(1);
        assertThat(model.<Integer>get("data[0].categories[0].id")).isEqualTo(111);
        assertThat(model.<String>get("data[0].categories[0].link.params.rel")).isEqualTo("related");
        assertThat(model.<String>get("data[0].categories[0].link.href")).isEqualTo("http://localhost:9998/categories/111");
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(LinkInfo.REF_SELF);
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/comtasks/31");
    }
}
