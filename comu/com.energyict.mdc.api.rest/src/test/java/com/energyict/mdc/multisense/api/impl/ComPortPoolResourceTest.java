package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.jayway.jsonpath.JsonModel;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.Optional;
import javax.ws.rs.core.Response;
import net.minidev.json.JSONObject;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 7/14/15.
 */
public class ComPortPoolResourceTest extends MultisensePublicApiJerseyTest {

    @Test
    public void testGetSingleComPortPool() throws Exception {
        mockComPortPool(11L, "cpp1");
        Response response = target("/comportpools/11").request().get();
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<JSONObject>get("$").size()).isEqualTo(6);
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(11);
        assertThat(jsonModel.<String>get("$.link.href")).isEqualTo("http://localhost:9998/comportpools/11");
        assertThat(jsonModel.<String>get("$.link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("cpp1");
        assertThat(jsonModel.<Boolean>get("$.active")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.description")).isEqualTo("Just another comportpool");
        assertThat(jsonModel.<String>get("$.type")).isEqualTo("TCP");

    }

    private ComPortPool mockComPortPool(long id, String name) {
        ComPortPool mock = mock(ComPortPool.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        when(mock.getComPorts()).thenReturn(Collections.emptyList());
        when(mock.getComPortType()).thenReturn(ComPortType.TCP);
        when(mock.getDescription()).thenReturn("Just another comportpool");
        when(mock.isActive()).thenReturn(true);
        doReturn(Optional.of(mock)).when(engineConfigurationService).findComPortPool(id);
        return mock;
    }
}
